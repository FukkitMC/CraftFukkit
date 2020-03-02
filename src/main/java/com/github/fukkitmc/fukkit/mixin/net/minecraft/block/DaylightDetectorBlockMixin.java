package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DaylightDetectorBlock.class)
public class DaylightDetectorBlockMixin {
	@ModifyVariable(method = "updateState", index = 3, ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private static int fukkit_redstoneChange(int i, BlockState state, World world, BlockPos pos) {
		return CraftEventFactory.callRedstoneChange(world, pos, state.get(DaylightDetectorBlock.POWER), i).getNewCurrent();
	}
}
