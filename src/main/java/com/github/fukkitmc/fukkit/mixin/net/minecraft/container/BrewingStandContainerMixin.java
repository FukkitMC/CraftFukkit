package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.BrewingStandContainer;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryBrewer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BrewingStandContainer.class)
public abstract class BrewingStandContainerMixin implements CommonContainerAccess {
	@Shadow @Final private Inventory inventory;

	@Override
	public CraftInventory createInventory() {
		return new CraftInventoryBrewer(this.inventory);
	}
}
