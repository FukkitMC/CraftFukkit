package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.BlockAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Implements (@Interface (iface = BlockAccess.class, prefix = "fukkit$"))
@Mixin (SpawnerBlock.class)
public class SpawnerBlockMixin extends Block {
	public SpawnerBlockMixin(Settings settings) {
		super(settings);
	}

	/**
	 * @author HalfOf2
	 * @reason redirect to super, get exp handled
	 */
	@Override
	@Overwrite
	public void onStacksDropped(BlockState state, World world, BlockPos pos, ItemStack stack) {
		super.onStacksDropped(state, world, pos, stack);
	}

	public int fukkit$getExpDrop(BlockState state, World world, BlockPos pos, ItemStack stack) {
		return 15 + world.random.nextInt(15) + world.random.nextInt(15);
	}
}
