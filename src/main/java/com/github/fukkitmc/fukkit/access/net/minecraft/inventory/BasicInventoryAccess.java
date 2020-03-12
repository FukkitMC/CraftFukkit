package com.github.fukkitmc.fukkit.access.net.minecraft.inventory;

import org.bukkit.inventory.InventoryHolder;

public interface BasicInventoryAccess extends InventoryAccess {
	void setOwner(InventoryHolder holder);
}
