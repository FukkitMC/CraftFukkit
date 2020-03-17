package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (ConduitBlockEntity.class)
public class ConduitBlockEntityMixin extends BlockEntity {
	@Shadow @Nullable private LivingEntity targetEntity;

	public ConduitBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Redirect (method = "givePlayersEffects",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/entity/player/PlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
	private boolean fukkit_addEffect(PlayerEntity entity, StatusEffectInstance effect) {
		return ((PlayerEntityAccess<?>) entity).addEffect(effect, EntityPotionEffectEvent.Cause.CONDUIT);
	}

	@Redirect(method = "attackHostileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
	private void fukkit_onlyPlayOnDamage(World world, PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
	}

	@Redirect(method = "attackHostileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean fukkit_blockDamage(LivingEntity entity, DamageSource source, float amount) {
		CraftEventFactory.blockDamage = CraftBlock.at(this.world, this.pos);
		if(entity.damage(source, amount)) {
			this.world.playSound(null, this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
		CraftEventFactory.blockDamage = null;
		return false;
	}
}
