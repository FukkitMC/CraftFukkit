package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.GourdBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Random;

@Mixin(StemBlock.class)
public class StemBlockMixin {
	@Shadow @Final private GourdBlock gourdBlock;

	@Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_growEvent(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
	}

	@Inject (method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_growEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci, Direction direction, BlockPos pos1) {
		if(!CraftEventFactory.handleBlockGrowEvent(world, pos1, this.gourdBlock.getDefaultState()))
			ci.cancel();
	}

	@Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 0))
	private boolean fukkit_growEvent(ServerWorld world, BlockPos pos, BlockState blockState) {
		return false;
	}

	@Redirect(method = "grow", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_growEvent2(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
	}
}
