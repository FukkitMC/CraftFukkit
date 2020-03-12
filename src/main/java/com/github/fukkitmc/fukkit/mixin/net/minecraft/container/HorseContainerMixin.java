package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.container.HopperContainer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HopperContainer.class)
public abstract class HorseContainerMixin implements CommonContainerAccess {
	@Override
	public CraftInventory createInventory() {
		return (CraftInventory) ((InventoryAccess)this.getInventory()).getOwner().getInventory();
	}
}
