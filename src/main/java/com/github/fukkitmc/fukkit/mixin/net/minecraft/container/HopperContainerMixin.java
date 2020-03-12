package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.HopperContainer;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HopperContainer.class)
public abstract class HopperContainerMixin implements CommonContainerAccess {
	@Shadow @Final private Inventory inventory;

	@Override
	public CraftInventory createInventory() {
		return new CraftInventory(this.inventory);
	}
}
