package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CoralFanBlock;
import net.minecraft.block.CoralWallFanBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(CoralWallFanBlock.class)
public class CoralWallFanBlockMixin {
	@Shadow @Final private Block deadCoralBlock;

	@Inject (method = "scheduledTick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private void fukkit_fadeEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if(CraftEventFactory.callBlockFadeEvent(world, pos, this.deadCoralBlock.getDefaultState().with(CoralWallFanBlock.WATERLOGGED, false).with(CoralWallFanBlock.FACING, state.get(CoralWallFanBlock.FACING))).isCancelled())
			ci.cancel();
	}
}
