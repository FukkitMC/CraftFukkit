package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (NetherWartBlock.class)
public class NetherWartBlockMixin {
	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                        "(Lnet/minecraft/util/math/BlockPos;" +
	                                                        "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_growEvent(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
	}
}
