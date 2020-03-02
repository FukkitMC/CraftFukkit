package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComparatorBlock.class)
public class ComparatorBlockMixin {
	@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 0))
	private void fukkit_depowerEvent0(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
		if(CraftEventFactory.callRedstoneChange(world, pos, 15, 0).getNewCurrent() != 0)
			ci.cancel();
	}

	@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 1))
	private void fukkit_depowerEvent1(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
		if(CraftEventFactory.callRedstoneChange(world, pos, 0, 15).getNewCurrent() != 15)
			ci.cancel();
	}
}
