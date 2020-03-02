package com.github.fukkitmc.fukkit.mixin.net.minecraft.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Random;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
	@Redirect (method = "onRandomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
	public boolean fukkit_voidSet(World world, BlockPos pos, BlockState blockState) {
		return false;
	}

	@Inject (method = "onRandomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	public void fukkit_blockIgniteEvent(World world, BlockPos blockPos, FluidState arg2, Random random, CallbackInfo ci, int i, BlockPos blockPos2, int j, BlockState blockState, World var9, BlockPos var10, BlockState var11) {
		if(world.getBlockState(blockPos2).getBlock() != Blocks.FIRE)
			if(!CraftEventFactory.callBlockIgniteEvent(world, blockPos2, blockPos).isCancelled())
				world.setBlockState(blockPos2, Blocks.FIRE.getDefaultState());
	}

	@Inject (method = "onRandomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
	public void fukkit_blockIgniteEvent(World world, BlockPos blockPos, FluidState fluidState, Random random, CallbackInfo ci, int i, int k, BlockPos blockPos3, World var9, BlockPos var10, BlockState var11) {
		BlockPos up = blockPos3.up();
		if(world.getBlockState(up).getBlock() != Blocks.FIRE)
			if(!CraftEventFactory.callBlockIgniteEvent(world, up, blockPos).isCancelled())
				world.setBlockState(up, Blocks.FIRE.getDefaultState());
	}

}
