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

@Implements (@Interface (iface = InventoryAccess.class, prefix = "fukkit$"))
@Mixin (CraftingResultInventory.class)
public abstract class CraftingResultInventoryMixin implements Inventory {
	@Shadow @Final private DefaultedList<ItemStack> stack;
	private int maxStack = MAX_STACK;

	public java.util.List<ItemStack> fukkit$getContents() {
		return this.stack;
	}

	// Don't need a transaction; the InventoryCrafting keeps track of it for us
	public void fukkit$onOpen(CraftHumanEntity who) {}

	public void fukkit$onClose(CraftHumanEntity who) {}

	public org.bukkit.inventory.InventoryHolder fukkit$getOwner() {
		return null; // Result slots don't get an owner
	}

	public void fukkit$setMaxStackSize(int size) {
		this.maxStack = size;
	}

	public Location fukkit$getLocation() {
		return null;
	}

	public java.util.List<HumanEntity> getViewers() {
		return new java.util.ArrayList<>();
	}

	@Override
	public int getInvMaxStackAmount() {
		return this.maxStack;
	}
}
