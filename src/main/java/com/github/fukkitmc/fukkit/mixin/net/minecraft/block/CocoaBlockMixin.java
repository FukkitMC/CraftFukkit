package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (CocoaBlock.class)
public class CocoaBlockMixin {
	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                        "(Lnet/minecraft/util/math/BlockPos;" +
	                                                        "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_grow0(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
	}

	@Redirect (method = "grow", at = @At (value = "INVOKE",
	                                      target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                               "(Lnet/minecraft/util/math/BlockPos;" +
	                                               "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_grow1(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
	}
}
