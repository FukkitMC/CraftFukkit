package com.github.fukkitmc.fukkit.access.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface AbstractFurnaceBlockEntityAccess extends InventoryAccess {
	void dropExperience(PlayerEntity entityhuman, ItemStack itemstack, int amount);
}
