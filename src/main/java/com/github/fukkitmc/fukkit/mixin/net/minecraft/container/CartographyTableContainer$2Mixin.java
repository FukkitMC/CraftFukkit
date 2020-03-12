package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.container.CartographyTableContainer;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin (targets = "net/minecraft/container/CartographyTableContainer$2")
public abstract class CartographyTableContainer$2Mixin implements InventoryAccess {
	@SuppressWarnings ("ShadowTarget") @Shadow @Final
	private CartographyTableContainer field_19273;

	@Override
	public Location getLocation() {
		return ((BlockContextAccess) ((BlockContextContainerAccess) this.field_19273).getContext()).getLocation();
	}
}
