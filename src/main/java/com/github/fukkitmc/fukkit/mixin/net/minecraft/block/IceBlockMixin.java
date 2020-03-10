package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (IceBlock.class)
public class IceBlockMixin {
	@Inject (method = "melt", at = @At (value = "HEAD"), cancellable = true)
	private void fukkit_fadeEvent(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		if (CraftEventFactory.callBlockFadeEvent(world, pos,
		world.dimension.doesWaterVaporize() ? Blocks.AIR.getDefaultState() : Blocks.WATER.getDefaultState())
		                     .isCancelled()) { ci.cancel(); }
	}
}
