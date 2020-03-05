package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin {
	@Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 0), cancellable = true)
	private void fukkit_fadeEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if(CraftEventFactory.callBlockFadeEvent(world, pos, Blocks.DIRT.getDefaultState()).isCancelled())
			ci.cancel();
	}

	@Redirect (method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 1))
	private boolean fukkit_fadeEvent2(ServerWorld world, BlockPos pos1, BlockState blockState, BlockState state, ServerWorld world1, BlockPos pos, Random random) {
		return CraftEventFactory.handleBlockSpreadEvent(world, pos, pos1, blockState);
	}
}
