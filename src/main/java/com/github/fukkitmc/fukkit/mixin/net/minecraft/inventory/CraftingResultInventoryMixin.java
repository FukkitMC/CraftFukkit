package com.github.fukkitmc.fukkit.mixin.net.minecraft.inventory;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.*;

import static com.github.fukkitmc.fukkit.util.Constants.MAX_STACK;

@Mixin (CraftingResultInventory.class)
public abstract class CraftingResultInventoryMixin implements InventoryAccess, Inventory {
	@Shadow @Final private DefaultedList<ItemStack> stack;
	private int maxStack = MAX_STACK;

	@Override public java.util.List<ItemStack> getContents() {
		return this.stack;
	}

	// Don't need a transaction; the InventoryCrafting keeps track of it for us
	@Override public void onOpen(CraftHumanEntity who) {}

	@Override public void onClose(CraftHumanEntity who) {}

	@Override public org.bukkit.inventory.InventoryHolder getOwner() {
		return null; // Result slots don't get an owner
	}

	@Override public void setMaxStackSize(int size) {
		this.maxStack = size;
	}

	@Override public Location getLocation() {
		return null;
	}

	@Override public java.util.List<HumanEntity> getViewers() {
		return new java.util.ArrayList<>();
	}

	@Override
	public int getInvMaxStackAmount() {
		return this.maxStack;
	}
}
