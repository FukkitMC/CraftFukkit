package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.container.GrindstoneContainer;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin (targets = "net/minecraft/container/GrindstoneContainer$1")
public abstract class GrindstoneContainer$1Mixin implements InventoryAccess {
	@SuppressWarnings ("ShadowTarget") @Shadow @Final GrindstoneContainer field_16776;

	@Override
	public Location getLocation() {
		return ((BlockContextAccess)((BlockContextContainerAccess) this.field_16776).getContext()).getLocation();
	}
}
