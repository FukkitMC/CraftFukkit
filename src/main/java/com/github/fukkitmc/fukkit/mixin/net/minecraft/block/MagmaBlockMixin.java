package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.MagmaBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MagmaBlock.class)
public class MagmaBlockMixin {
	@Inject(method = "onSteppedOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private void fukkit_blockDamage(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		CraftEventFactory.blockDamage = ((WorldAccess)world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
	}

	@Inject(method = "onSteppedOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", shift = At.Shift.AFTER))
	private void fukkit_nullify(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		CraftEventFactory.blockDamage = null;
	}
}
