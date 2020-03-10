package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.container.CartographyTableContainer;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryCartography;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CartographyTableContainer.class)
public abstract class CartographyTableContainerMixin implements CommonContainerAccess, BlockContextContainerAccess {
	@Shadow @Final public Inventory inventory;

	@Shadow @Final private CraftingResultInventory resultSlot;

	@Shadow @Final private BlockContext context;

	@Override
	public CraftInventory createInventory() {
		return new CraftInventoryCartography(this.inventory, this.resultSlot);
	}

	@Override
	public BlockContext getContext() {
		return this.context;
	}
}
