package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.CraftingInventoryAccess;
import com.github.fukkitmc.fukkit.util.Constants;
import com.github.fukkitmc.fukkit.util.Constructors;
import net.minecraft.container.PlayerContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (PlayerContainer.class)
public class PlayerContainerMixin extends ContainerMixin implements ContainerAccess {
	private CraftingInventory craftingInventory;
	private CraftingResultInventory craftingResultInventory;
	private CraftInventoryView bukkitEntity = null;
	private PlayerInventory player;

	@Inject (method = "<init>", at = @At ("TAIL"))
	private void fukkit_moved(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
		this.craftingResultInventory = new CraftingResultInventory(); // CraftBukkit - moved to before InventoryCrafting construction
		this.craftingInventory = Constructors.newCraftingInventory((PlayerContainer) (Object) this, 2, 2, inventory.player); // CraftBukkit - pass player
		((CraftingInventoryAccess) this.craftingInventory).setResultInventory(this.craftingResultInventory);
		this.player = inventory; // CraftBukkit - save player
		this.setTitle(new TranslatableText("container.crafting")); // SPIGOT-4722: Allocate title for player inventory
	}

	@Redirect(method = "onContentChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/CraftingTableContainer;updateResult(ILnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/inventory/CraftingResultInventory;)V"))
	private void fukkit_update(int syncId, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory) {
		Constants.updateResult(syncId, world, player, craftingInventory, resultInventory, (PlayerContainer) (Object) this);
	}

	@Override public InventoryView getBukkitView() {
		if (this.bukkitEntity != null) {
			return this.bukkitEntity;
		}

		CraftInventoryCrafting inventory = new CraftInventoryCrafting(this.craftingInventory, this.craftingResultInventory);
		this.bukkitEntity = new CraftInventoryView(((PlayerEntityAccess<?>) this.player.player).getBukkit(), inventory, (PlayerContainer) (Object) this);
		return this.bukkitEntity;
	}
}
