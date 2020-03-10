package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (FluidBlock.class)
public abstract class FluidBlockMixin {
	// two birds 1 stone
	@Redirect (method = "receiveNeighborFluids", at = @At (value = "INVOKE",
	                                                       target = "Lnet/minecraft/world/World;setBlockState" +
	                                                                "(Lnet/minecraft/util/math/BlockPos;" +
	                                                                "Lnet/minecraft/block/BlockState;)Z"))
	private boolean fukkit_formEvent(World world, BlockPos pos, BlockState blockState) {
		if (CraftEventFactory.handleBlockFormEvent(world, pos, blockState)) {
			this.playExtinguishSound(world, pos);
		}
		return false;
	}

	@Shadow protected abstract void playExtinguishSound(IWorld world, BlockPos pos);

	// two birds 1 stone
	@Redirect (method = "receiveNeighborFluids", at = @At (value = "INVOKE",
	                                                       target = "Lnet/minecraft/block/FluidBlock;" +
	                                                                "playExtinguishSound(Lnet/minecraft/world/IWorld;" +
	                                                                "Lnet/minecraft/util/math/BlockPos;)V"))
	private void fukkit_voidCall(FluidBlock block, IWorld world, BlockPos pos) {
	}
}
