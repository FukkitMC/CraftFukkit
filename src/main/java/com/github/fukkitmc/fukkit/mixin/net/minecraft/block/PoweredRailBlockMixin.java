package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PoweredRailBlock.class)
public class PoweredRailBlockMixin {
	@Inject(method = "updateBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void fukkit_redstoneChange(BlockState state, World world, BlockPos pos, Block neighbor, CallbackInfo ci, boolean powered) {
		int power = powered ? 15 : 0;
		int newPower = CraftEventFactory.callRedstoneChange(world, pos, power, 15 - power).getNewCurrent();
		if(power == newPower)
			ci.cancel();
	}
}
