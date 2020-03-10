package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BambooSaplingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (BambooSaplingBlock.class)
public class BambooSaplingBlockMixin {
	@Redirect (method = "grow(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;" +
	                              "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_spreadEvent(World world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockSpreadEvent(world, pos.down(), pos, state, 3);
	}
}
