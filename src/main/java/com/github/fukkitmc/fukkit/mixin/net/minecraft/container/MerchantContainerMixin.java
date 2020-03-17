package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.MerchantContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.village.Trader;
import net.minecraft.village.TraderInventory;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (MerchantContainer.class)
public class MerchantContainerMixin extends ContainerMixin implements ContainerAccess {
	@Shadow @Final private Trader trader;
	@Shadow @Final private TraderInventory traderInventory;
	private CraftInventoryView bukkitEntity = null;
	private PlayerInventory player;

	@Inject (method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/village/Trader;)V", at = @At ("TAIL"))
	private void fukkit_savePlayer(int syncId, PlayerInventory playerInventory, Trader trader, CallbackInfo ci) {
		this.player = playerInventory;
	}

	@Override
	public CraftInventoryView getBukkitView() {
		if (this.bukkitEntity == null) {
			this.bukkitEntity = new CraftInventoryView(((PlayerEntityAccess<?>) this.player.player).getBukkit(), new org.bukkit.craftbukkit.inventory.CraftInventoryMerchant(this.trader, this.traderInventory), (MerchantContainer) (Object) this);
		}
		return this.bukkitEntity;
	}

	@Redirect (method = "playYesSound", at = @At (value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z"))
	private boolean fukkit_Spigot5035(World world) {
		return world.isClient || !(this.trader instanceof Entity);
	}
}
