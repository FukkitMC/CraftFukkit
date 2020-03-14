package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.GenericContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (GenericContainer.class)
public abstract class GenericContainerMixin implements ContainerAccess {
	@Shadow @Final private Inventory inventory;

	private CraftInventoryView view;
	public PlayerInventory playerInv;

	@Inject (
	method = "<init>(Lnet/minecraft/container/ContainerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V",
	at = @At ("TAIL"))
	private void fukkit_initPlayer(ContainerType<?> containerType, int syncId, PlayerInventory playerInventory,
	                               Inventory inventory, int rows, CallbackInfo ci) {
		this.playerInv = playerInventory;
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) this.playerInv.player).getBukkit(), this
		                                                                                           .createInventory(),
		                                   (Container) (Object) this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getCheckReachable()) { cir.setReturnValue(true); }
	}

	public CraftInventory createInventory() {
		CraftInventory inventory;
		if (this.inventory instanceof PlayerInventory) {
			inventory = new org.bukkit.craftbukkit.inventory.CraftInventoryPlayer(this.playerInv);
		} else if (this.inventory instanceof DoubleInventory) {

			inventory = null; // fixme odd compile error
			//new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((DoubleInventory) this.inventory);
		} else {
			inventory = new CraftInventory(this.playerInv);
		}
		return inventory;
	}
}
