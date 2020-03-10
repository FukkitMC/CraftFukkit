package com.github.fukkitmc.fukkit.mixin.net.minecraft.inventory;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.inventory.EnderChestInventory;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin (EnderChestInventory.class)
public class EnderChestInventoryMixin extends BasicInventoryMixin {
	@Shadow private EnderChestBlockEntity currentBlockEntity;

	@Override
	public Location fukkit$getLocation() {
		return new Location(((WorldAccess) this.currentBlockEntity.getWorld()).getBukkit(), this.currentBlockEntity
		                                                                                    .getPos()
		                                                                                    .getX(), this.currentBlockEntity
		                                                                                             .getPos()
		                                                                                             .getY(), this.currentBlockEntity
		                                                                                                      .getPos()
		                                                                                                      .getZ());
	}
}
