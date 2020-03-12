package com.github.fukkitmc.fukkit.access.net.minecraft.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;

public interface CommonContainerAccess extends ContainerAccess {
	CraftInventory createInventory();
	void setInventory(PlayerInventory inventory);
	Player getPlayer();
	PlayerInventory getInventory();
}
