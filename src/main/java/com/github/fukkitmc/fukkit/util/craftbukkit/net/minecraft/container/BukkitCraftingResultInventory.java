package com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.LocationInventoryAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.inventory.CraftingResultInventory;
import org.bukkit.Location;

public class BukkitCraftingResultInventory extends CraftingResultInventory implements LocationInventoryAccess {
	private final Container container;
	private final BlockContext context;

	public BukkitCraftingResultInventory(Container container, BlockContext context) {
		this.container = container;
		this.context = context;
	}

	@Override
	public void markDirty() {
		this.container.onContentChanged(this);
		super.markDirty();
	}

	@Override
	public Location getLocation() {
		return ((BlockContextAccess) this.context).getLocation();
	}
}
