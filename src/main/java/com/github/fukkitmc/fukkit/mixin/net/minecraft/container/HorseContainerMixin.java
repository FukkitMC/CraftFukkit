package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.container.Container;
import net.minecraft.container.HopperContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (HopperContainer.class)
public abstract class HorseContainerMixin extends ContainerMixin implements ContainerAccess {
	private CraftInventoryView view;
	public PlayerInventory playerInventory;

	@Inject (
	method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V",
	at = @At ("TAIL"))
	private void fukkit_initPlayer(int syncId, PlayerInventory playerInventory, Inventory inventory, CallbackInfo ci) {
		this.playerInventory = playerInventory;
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) this.playerInventory.player).getBukkit(), ((InventoryAccess) this.playerInventory).getOwner().getInventory(),
		                                   (Container) (Object) this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getCheckReachable()) { cir.setReturnValue(true); }
	}
}
