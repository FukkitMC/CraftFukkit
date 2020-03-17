package com.github.fukkitmc.fukkit.mixin.net.minecraft.inventory;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.ArrayList;
import java.util.List;

@Mixin (DoubleInventory.class)
public abstract class DoubleInventoryMixin implements InventoryAccess {
	public List<HumanEntity> transaction = new java.util.ArrayList<>();
	@Shadow @Final private Inventory first;
	@Shadow @Final private Inventory second;

	@Override public List<ItemStack> getContents() {
		List<ItemStack> result = new ArrayList<>(this.getInvSize());
		for (int i = 0; i < this.getInvSize(); i++) {
			result.add(this.getInvStack(i));
		}
		return result;
	}

	@Shadow public abstract int getInvSize();

	@Shadow public abstract ItemStack getInvStack(int slot);

	@Override public void onOpen(CraftHumanEntity who) {
		((InventoryAccess) this.first).onOpen(who);
		((InventoryAccess) this.second).onOpen(who);
		this.transaction.add(who);
	}

	@Override public void onClose(CraftHumanEntity who) {
		((InventoryAccess) this.first).onClose(who);
		((InventoryAccess) this.second).onClose(who);
		this.transaction.remove(who);
	}

	@Override public List<HumanEntity> getViewers() {
		return this.transaction;
	}

	@Override public org.bukkit.inventory.InventoryHolder getOwner() {
		return null; // This method won't be called since CraftInventoryDoubleChest doesn't defer to here
	}

	@Override public void setMaxStackSize(int size) {
		((InventoryAccess) this.first).setMaxStackSize(size);
		((InventoryAccess) this.second).setMaxStackSize(size);
	}

	public Location fukkit$getLocation() {
		return ((InventoryAccess) this.first).getLocation();
	}

	@Redirect (method = "getInvMaxStackAmount",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;getInvMaxStackAmount()I"))
	private int fukkit_checkBoth(Inventory inventory) {
		return Math.min(inventory.getInvMaxStackAmount(), this.second.getInvMaxStackAmount());
	}
}
