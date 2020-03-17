package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.Container;
import net.minecraft.container.ShulkerBoxContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (ShulkerBoxContainer.class)
public class ShulkerBoxContainerMixin extends ContainerMixin implements ContainerAccess {
	@Shadow @Final private Inventory inventory;
	private CraftInventoryView bukkitEntity;
	private PlayerInventory player;

	@Inject (method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At ("TAIL"))
	private void fukkit_savePlayer(int syncId, PlayerInventory playerInventory, Inventory inventory, CallbackInfo ci) {
		this.player = playerInventory;
	}

	@Override public InventoryView getBukkitView() {
		if (this.bukkitEntity != null) {
			return this.bukkitEntity;
		}

		this.bukkitEntity = new CraftInventoryView(((PlayerEntityAccess<?>)this.player.player).getBukkit(), new CraftInventory(this.inventory), (Container) (Object) this);
		return this.bukkitEntity;
	}
}
