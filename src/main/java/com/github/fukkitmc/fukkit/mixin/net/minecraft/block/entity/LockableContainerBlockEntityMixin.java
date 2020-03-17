package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;

@Mixin (LockableContainerBlockEntity.class) public abstract class LockableContainerBlockEntityMixin extends BlockEntityMixin implements InventoryAccess {

	@Override public Location getLocation() {
		if (this.world == null) return null;
		return new org.bukkit.Location(((WorldAccess) this.world).getBukkit(), this.pos.getX(), this.pos.getY(), this.pos.getZ());
	}
}
