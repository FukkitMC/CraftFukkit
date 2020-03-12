package com.github.fukkitmc.fukkit.mixin.net.minecraft.inventory;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.BasicInventoryAccess;
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

@Mixin (BasicInventory.class)
public class BasicInventoryMixin implements BasicInventoryAccess {
	public List<HumanEntity> viewers = new ArrayList<>();
	@Shadow
	@Final
	private DefaultedList<ItemStack> stackList;
	private InventoryHolder holder;
	private int maxStack = Constants.MAX_STACK;

	@Override
	public List<ItemStack> getContents() {
		return this.stackList;
	}

	@Override
	public void onOpen(CraftHumanEntity who) {
		this.viewers.add(who);
	}

	@Override
	public void onClose(CraftHumanEntity who) {
		this.viewers.remove(who);
	}

	@Override
	public List<HumanEntity> getViewers() {
		return this.viewers;
	}

	@Override
	public InventoryHolder getOwner() {
		return this.holder;
	}

	@Override
	public void setOwner(InventoryHolder holder) {
		this.holder = holder;
	}

	@Override
	public void setMaxStackSize(int size) {
		this.maxStack = size;
	}

	@Override
	public Location getLocation() {
		return null;
	}
}
