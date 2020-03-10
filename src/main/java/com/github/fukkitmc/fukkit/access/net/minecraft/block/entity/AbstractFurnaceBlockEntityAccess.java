package com.github.fukkitmc.fukkit.access.net.minecraft.block.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface AbstractFurnaceBlockEntityAccess {
	void dropExperience(PlayerEntity entityhuman, ItemStack itemstack, int amount);

	void dropExperience(PlayerEntity entityhuman, int i, float f, ItemStack itemstack, int amount);
}
