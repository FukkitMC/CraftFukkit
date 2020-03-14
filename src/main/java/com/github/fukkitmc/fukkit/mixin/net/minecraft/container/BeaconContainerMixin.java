package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.BeaconContainer;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.PropertyDelegate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (BeaconContainer.class)
public abstract class BeaconContainerMixin implements ContainerAccess {
	@Shadow @Final private Inventory paymentInv;

	private CraftInventoryView view;
	public PlayerInventory playerInventory;

	@Inject (
	method = "<init>(ILnet/minecraft/inventory/Inventory;Lnet/minecraft/container/PropertyDelegate;Lnet/minecraft/container/BlockContext;)V",
	at = @At ("TAIL"))
	private void fukkit_initPlayer(int syncId, Inventory inventory, PropertyDelegate propertyDelegate, BlockContext blockContext, CallbackInfo ci) {
		this.playerInventory = (PlayerInventory) inventory;
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) playerInventory.player).getBukkit(), new CraftInventoryBeacon(this.paymentInv),
		                                   (Container) (Object) this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getCheckReachable()) { cir.setReturnValue(true); }
	}
}
