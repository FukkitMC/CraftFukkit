package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.Generic3x3Container;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Generic3x3Container.class)
public abstract class Generic3x3ContainerMixin implements CommonContainerAccess {
	@Shadow @Final private Inventory inventory;

	@Override
	public CraftInventory createInventory() {
		return new CraftInventory(this.inventory);
	}
}
