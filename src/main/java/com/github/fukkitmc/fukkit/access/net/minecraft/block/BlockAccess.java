package com.github.fukkitmc.fukkit.access.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockAccess {
	int getExpDrop(BlockState state, World world, BlockPos pos, ItemStack stack);
}
