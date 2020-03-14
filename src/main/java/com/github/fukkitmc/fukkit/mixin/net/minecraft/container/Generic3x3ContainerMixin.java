package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.Container;
import net.minecraft.container.Generic3x3Container;
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

@Mixin(Generic3x3Container.class)
public abstract class Generic3x3ContainerMixin extends ContainerMixin implements ContainerAccess {
	@Shadow @Final private Inventory inventory;
	private CraftInventoryView view;
	public PlayerInventory playerInventory;

	@Inject (
	method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V",
	at = @At ("TAIL"))
	private void fukkit_initPlayer(int syncId, PlayerInventory playerInventory, Inventory inventory, CallbackInfo ci) {
		this.playerInventory = (PlayerInventory) inventory;
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) playerInventory.player).getBukkit(), new CraftInventory(this.inventory),
		                                   (Container) (Object) this);
		return this.view;
	}
}
