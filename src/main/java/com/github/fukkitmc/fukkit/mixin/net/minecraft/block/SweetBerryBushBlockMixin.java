package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {
	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                        "(Lnet/minecraft/util/math/BlockPos;" +
	                                                        "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_growEvent(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
	}

	@Inject (method = "onEntityCollision", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/entity/Entity;damage" +
	                                                          "(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private void fukkit_damage(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		CraftEventFactory.blockDamage = CraftBlock.at(world, pos);
	}

	@Inject (method = "onEntityCollision", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/entity/Entity;damage" +
	                                                          "(Lnet/minecraft/entity/damage/DamageSource;F)Z",
	                                                 shift = At.Shift.AFTER))
	private void fukkit_damageReset(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		CraftEventFactory.blockDamage = null;
	}
}
