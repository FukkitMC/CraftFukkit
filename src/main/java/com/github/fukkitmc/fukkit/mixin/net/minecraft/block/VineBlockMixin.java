package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.VineBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Random;

@Mixin(VineBlock.class)
public class VineBlockMixin {
	// not exact but 99% sure it's the same effect
	// 7 birds 1 stone
	@Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_spreadEvent(ServerWorld world, BlockPos dest, BlockState state, int flags, BlockState state1, ServerWorld world1, BlockPos src, Random random) {
		if(dest == src)
			return world.setBlockState(dest, state, flags);
		return CraftEventFactory.handleBlockSpreadEvent(world, src, dest, state, flags);
	}
}
