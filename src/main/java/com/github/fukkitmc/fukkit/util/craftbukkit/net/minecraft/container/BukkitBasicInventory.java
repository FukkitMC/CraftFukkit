package com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.LocationInventoryAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.item.ItemStack;
import org.bukkit.Location;

// todo stop lazy and implement as mixin to inner classes
public class BukkitBasicInventory extends BasicInventory implements LocationInventoryAccess {
	private final BlockContextAccess context;
	private final Container container;
	public BukkitBasicInventory(int size, BlockContext context, Container container) {
		super(size);
		this.context = (BlockContextAccess) context;
		this.container = container;
	}

	public BukkitBasicInventory(BlockContext context, Container container, ItemStack... items) {
		super(items);
		this.context = (BlockContextAccess) context;
		this.container = container;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		this.container.onContentChanged(this);
	}

	@Override
	public Location getLocation() {
		return this.context.getLocation();
	}
}
