package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.BeaconContainer;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryBeacon;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BeaconContainer.class)
public abstract class BeaconContainerMixin implements CommonContainerAccess {
	@Shadow @Final private Inventory paymentInv;

	@Override
	public CraftInventory createInventory() {
		return new CraftInventoryBeacon(this.paymentInv);
	}
}
