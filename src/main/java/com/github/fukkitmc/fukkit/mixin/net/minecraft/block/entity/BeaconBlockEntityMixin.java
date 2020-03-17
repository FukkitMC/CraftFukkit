package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.entity.BeaconBlockEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Box;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Iterator;
import java.util.List;

@Mixin (BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity implements BeaconBlockEntityAccess {
	@Shadow @Nullable private StatusEffect primary;

	@Shadow @Nullable private StatusEffect secondary;

	@Shadow private int level;

	public BeaconBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Override public PotionEffect getPrimaryEffect() {
		return (this.primary != null) ? CraftPotionUtil.toBukkit(new StatusEffectInstance(this.primary, this.getPower(), this.getAmplification(), true, true)) : null;
	}

	@Override public PotionEffect getSecondaryEffect() {
		return (this.hasSecondaryEffect()) ? CraftPotionUtil.toBukkit(new StatusEffectInstance(this.secondary, this.getPower(), this.getAmplification(), true, true)) : null;
	}

	private byte getAmplification() {
		byte amplification = 0;
		if (this.level >= 4 && this.primary == this.secondary) {
			amplification = 1;
		}
		return amplification;
	}

	private int getPower() {
		return (9 + this.level * 2) * 20;
	}

	@Override public List<PlayerEntity> getHumansInRange() {
		double beaconRange = this.level * 10 + 10;
		Box effectBounds = (new Box(this.pos)).expand(beaconRange).stretch(0.0D, this.world.getHeight(), 0.0D);
		return this.world.getNonSpectatingEntities(PlayerEntity.class, effectBounds);
	}

	private void applyEffect(List<PlayerEntity> list, StatusEffect effects, int i, int b0) {
		Iterator<PlayerEntity> iterator = list.iterator();
		PlayerEntity player;
		while (iterator.hasNext()) {
			player = iterator.next();
			((PlayerEntityAccess<?>) player).addEffect(new StatusEffectInstance(effects, i, b0, true, true), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.BEACON);
		}
	}

	private boolean hasSecondaryEffect() {
		return this.level >= 4 && this.primary != this.secondary && this.secondary != null;
	}

	/**
	 * todo is this actually needed?
	 *
	 * @author HalfOf2
	 * @reason splitting up of logic
	 */
	@Overwrite private void applyPlayerEffects() {
		if (!this.world.isClient && this.primary != null) {
			byte amplification = this.getAmplification();

			int pyramidSize = this.getPower();
			List<PlayerEntity> list = this.getHumansInRange();

			this.applyEffect(list, this.primary, pyramidSize, amplification);

			if (this.hasSecondaryEffect()) {
				this.applyEffect(list, this.secondary, pyramidSize, 0);
			}
		}
	}

	@Redirect(method = "fromTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BeaconBlockEntity;getPotionEffectById(I)Lnet/minecraft/entity/effect/StatusEffect;"))
	private StatusEffect fukkit_spigot3598(int id) {
		return StatusEffect.byRawId(id);
	}

	@Inject(method = "fromTag", at = @At(value = "JUMP"))
	private void fukkit_5053(CompoundTag tag, CallbackInfo ci) {
		this.level = tag.getInt("Levels");
	}
}
