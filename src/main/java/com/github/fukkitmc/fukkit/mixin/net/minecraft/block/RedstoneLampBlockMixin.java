package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin (RedstoneLampBlock.class)
public class RedstoneLampBlockMixin {
	@Inject (method = "neighborUpdate", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/world/World;setBlockState" +
	                                                       "(Lnet/minecraft/util/math/BlockPos;" +
	                                                       "Lnet/minecraft/block/BlockState;I)Z"),
	         cancellable = true)
	private void fukkit_redstoneChange(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos,
	                                   boolean moved, CallbackInfo ci) {
		if (CraftEventFactory.callRedstoneChange(world, pos, 0, 15).getNewCurrent() != 15) {
			ci.cancel();
		}
	}

	@Inject (method = "scheduledTick", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                      "(Lnet/minecraft/util/math/BlockPos;" +
	                                                      "Lnet/minecraft/block/BlockState;I)Z"),
	         cancellable = true)
	private void fukkit_redstoneChange(BlockState state, ServerWorld world, BlockPos pos, Random random,
	                                   CallbackInfo ci) {
		if (CraftEventFactory.callRedstoneChange(world, pos, 15, 0).getNewCurrent() != 0) {
			ci.cancel();
		}
	}
}
