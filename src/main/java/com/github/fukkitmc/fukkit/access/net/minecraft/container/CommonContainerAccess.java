package com.github.fukkitmc.fukkit.access.net.minecraft.container;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;

public interface CommonContainerAccess extends ContainerAccess {
	CraftInventory createInventory();
	void setPlayer(Player player);
}
