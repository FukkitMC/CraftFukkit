package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (TntBlock.class)
public class TntBlockMixin {
	@Inject (method = "onProjectileHit", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/block/TntBlock;primeTnt" +
	                                                        "(Lnet/minecraft/world/World;" +
	                                                        "Lnet/minecraft/util/math/BlockPos;" +
	                                                        "Lnet/minecraft/entity/LivingEntity;)V"),
	         cancellable = true)
	private void fukkit_entityChangeBlockEvent(World world, BlockState state, BlockHitResult hitResult, Entity entity,
	                                           CallbackInfo ci) {
		if (CraftEventFactory.callEntityChangeBlockEvent(entity, hitResult.getBlockPos(), Blocks.AIR.getDefaultState())
		                     .isCancelled()) { ci.cancel(); }
	}
}
