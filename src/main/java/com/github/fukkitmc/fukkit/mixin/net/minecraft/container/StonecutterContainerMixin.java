package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.StonecutterContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryStonecutter;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (StonecutterContainer.class)
public class StonecutterContainerMixin extends ContainerMixin implements ContainerAccess {
	@Shadow @Final public Inventory inventory;
	@Shadow @Final private CraftingResultInventory field_19173;
	private CraftInventoryView bukkitEntity = null;
	private Player player;

	@Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/container/BlockContext;)V", at = @At("TAIL"))
	private void fukkit_savePlayer(int syncId, PlayerInventory playerInventory, BlockContext blockContext, CallbackInfo ci) {
		this.player = ((PlayerEntityAccess<CraftPlayer>)playerInventory.player).getBukkit();
	}

	@Override public InventoryView getBukkitView() {
		if (this.bukkitEntity != null) {
			return this.bukkitEntity;
		}

		CraftInventoryStonecutter inventory = new CraftInventoryStonecutter(this.inventory, this.field_19173);
		this.bukkitEntity = new CraftInventoryView(this.player, inventory, (Container) (Object) this);
		return this.bukkitEntity;
	}
}
