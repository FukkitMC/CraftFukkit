package com.github.fukkitmc.fukkit.access.net.minecraft.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import java.util.List;

public interface InventoryAccess extends LocationInventoryAccess {
	List<ItemStack> getContents();

	void onOpen(CraftHumanEntity who);

	void onClose(CraftHumanEntity who);

	List<HumanEntity> getViewers();

	InventoryHolder getOwner();

	void setOwner(InventoryHolder holder);

	void setMaxStackSize(int size);

	default Recipe<?> getCurrentRecipe() { return null; }

	default void setCurrentRecipe(Recipe<?> recipe) {}
}
