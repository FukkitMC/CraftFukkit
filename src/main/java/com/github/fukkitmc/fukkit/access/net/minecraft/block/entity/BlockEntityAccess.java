package com.github.fukkitmc.fukkit.access.net.minecraft.block.entity;

import net.minecraft.world.World;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.inventory.InventoryHolder;

public interface BlockEntityAccess {
	CraftPersistentDataContainer getContainer();

	void setContainer(CraftPersistentDataContainer container);

	void setWorld(World world);
}
