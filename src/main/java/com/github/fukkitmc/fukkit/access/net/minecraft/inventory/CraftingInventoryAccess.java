package com.github.fukkitmc.fukkit.access.net.minecraft.inventory;

import net.minecraft.inventory.Inventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

public interface CraftingInventoryAccess extends InventoryAccess {
	InventoryType getInvType();

	Inventory getResultInventory();

	void setResultInventory(Inventory inventory);

	void setOwner(InventoryHolder holder);
}
