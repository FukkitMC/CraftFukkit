package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.BlockAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import java.util.Random;

@Implements(@Interface(iface = BlockAccess.class, prefix = "fukkit$"))
@Mixin(OreBlock.class)
public abstract class OreBlockMixin extends Block {
	public OreBlockMixin(Settings settings) {
		super(settings);
	}

	@Shadow protected abstract int getExperienceWhenMined(Random random);
	/**
	 * @author HalfOf2
	 * @reason delegated to get exp drop
	 */
	@Override
	@Overwrite
	public void onStacksDropped(BlockState state, World world, BlockPos pos, ItemStack stack) {
		super.onStacksDropped(state, world, pos, stack);
	}

	public int fukkit$getExpDrop(BlockState state, World world, BlockPos pos, ItemStack stack) {
		if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0) {
			int i = this.getExperienceWhenMined(world.random);

			if (i > 0) {
				return i;
			}
		}

		return 0;
	}
}
