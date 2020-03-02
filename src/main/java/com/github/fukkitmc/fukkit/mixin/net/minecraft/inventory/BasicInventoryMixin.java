package com.github.fukkitmc.fukkit.mixin.net.minecraft.inventory;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.*;
import java.util.ArrayList;
import java.util.List;

@Implements (@Interface (iface = InventoryAccess.class, prefix = "fukkit$"))
@Mixin (BasicInventory.class)
public class BasicInventoryMixin {
	@Shadow
	@Final
	private DefaultedList<ItemStack> stackList;
	public List<HumanEntity> viewers = new ArrayList<>();
	private InventoryHolder holder;
	private int maxStack = Constants.MAX_STACK;

	public List<ItemStack> fukkit$getContents() {
		return this.stackList;
	}

	public void fukkit$onOpen(CraftHumanEntity who) {
		this.viewers.add(who);
	}

	public void fukkit$onClose(CraftHumanEntity who) {
		this.viewers.remove(who);
	}

	public List<HumanEntity> fukkit$getViewers() {
		return this.viewers;
	}

	public InventoryHolder fukkit$getOwner() {
		return this.holder;
	}

	public void fukkit$setOwner(InventoryHolder holder) {
		this.holder = holder;
	}

	public void fukkit$setMaxStackSize(int size) {
		this.maxStack = size;
	}

	public Location fukkit$getLocation() {
		return null;
	}
}
