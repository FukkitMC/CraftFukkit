package com.github.fukkitmc.fukkit.access.net.minecraft.container;

import org.bukkit.craftbukkit.inventory.CraftInventory;

public interface CommonContainerAccess extends ContainerAccess {
	CraftInventory getInventory();
}
