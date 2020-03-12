package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.GrindstoneContainer;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GrindstoneContainer.class)
public abstract class GrindstoneContainerMixin implements CommonContainerAccess {
	@Shadow @Final private Inventory craftingInventory;

	@Shadow @Final private Inventory resultInventory;

	@Override
	public CraftInventory createInventory() {
		return new CraftInventoryCrafting(this.craftingInventory, this.resultInventory);
	}
}
