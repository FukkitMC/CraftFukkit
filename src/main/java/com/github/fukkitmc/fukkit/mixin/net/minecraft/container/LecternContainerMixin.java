package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.LecternContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.LecternContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryLectern;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (LecternContainer.class)
public abstract class LecternContainerMixin extends Container implements LecternContainerAccess {
	private CraftInventoryView view;
	public PlayerInventory playerInventory;
	@Shadow @Final private Inventory inventory;

	protected LecternContainerMixin(ContainerType<?> type, int syncId) {
		super(type, syncId);
	}


	@Inject (method = "onButtonClick", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/inventory/Inventory;removeInvStack(I)" +
	                                                      "Lnet/minecraft/item/ItemStack;"))
	private void fukkit_playerTakeLecternBookEvent(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
		PlayerTakeLecternBookEvent event = new PlayerTakeLecternBookEvent(((PlayerEntityAccess<CraftPlayer>) this.playerInventory.player).getBukkit(),
		                                                                  ((CraftInventoryLectern) this.getBukkitView()
		                                                                                               .getTopInventory())
		                                                                  .getHolder());
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
		}
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) this.playerInventory.player).getBukkit(), new CraftInventoryLectern(this.inventory), this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getCheckReachable()) { cir.setReturnValue(true); }
	}

	@Override
	public void setPlayerInv(PlayerInventory playerInv) {
		this.playerInventory = playerInv;
	}
}
