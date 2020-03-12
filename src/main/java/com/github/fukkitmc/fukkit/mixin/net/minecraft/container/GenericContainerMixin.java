package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.GenericContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin (GenericContainer.class)
public abstract class GenericContainerMixin implements CommonContainerAccess {
	@Shadow @Final private Inventory inventory;

	@Override
	public CraftInventory createInventory() {
		CraftInventory inventory;
		if (this.inventory instanceof PlayerInventory) {
			inventory = new org.bukkit.craftbukkit.inventory.CraftInventoryPlayer((PlayerInventory) this.inventory);
		} else if (this.inventory instanceof DoubleInventory) {
			inventory =
			new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((DoubleInventory) this.inventory);
		} else {
			inventory = new CraftInventory(this.inventory);
		}
		return inventory;
	}
}
