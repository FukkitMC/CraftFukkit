package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
	@Inject(method = "onProjectileHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_igniteEvent(World world, BlockState state, BlockHitResult hitResult, Entity entity, CallbackInfo ci, boolean bl2, BlockPos blockPos) {
		if(CraftEventFactory.callBlockIgniteEvent(world, blockPos, entity).isCancelled()) // TODO verify correct entitiy
			ci.cancel();
	}
}
