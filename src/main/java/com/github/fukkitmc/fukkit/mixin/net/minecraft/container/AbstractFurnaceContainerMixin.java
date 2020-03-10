package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.container.AbstractFurnaceContainer;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryFurnace;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceContainer.class)
public abstract class AbstractFurnaceContainerMixin implements CommonContainerAccess {
	@Shadow @Final private Inventory inventory;

	@Override
	public CraftInventory createInventory() {
		return new CraftInventoryFurnace(((AbstractFurnaceBlockEntity) this.inventory));
	}
}
