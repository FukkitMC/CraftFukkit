package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (
{AbstractFurnaceContainer.class, BeaconContainer.class, BrewingStandContainer.class, CartographyTableContainer.class,
 CraftingTableContainer.class, EnchantingTableContainer.class, GenericContainer.class, GrindstoneContainer.class,
 HopperContainer.class, HorseContainer.class
})
public abstract class CommonContainerMixin extends ContainerMixin implements CommonContainerAccess {
	private CraftInventoryView view;
	public PlayerInventory inventory;

	@Inject (
	method = "<init>(Lnet/minecraft/container/ContainerType;Lnet/minecraft/recipe/RecipeType;" +
	         "ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;" +
	         "Lnet/minecraft/container/PropertyDelegate;)V",
	at = @At ("TAIL"))
	private void fukkit_initPlayer(ContainerType<?> containerType,
	                               RecipeType<? extends AbstractCookingRecipe> recipeType, int syncId,
	                               PlayerInventory playerInventory, Inventory inventory,
	                               PropertyDelegate propertyDelegate, CallbackInfo ci) {
		this.inventory = playerInventory;
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) inventory.player).getBukkit(), this.createInventory(), (Container) (Object) this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.checkReachable) { cir.setReturnValue(true); }
	}

	@Override
	public void setInventory(PlayerInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public Player getPlayer() {
		return (Player) ((PlayerEntityAccess<?>) this.getInventory()).getBukkit();
	}

	@Override
	public PlayerInventory getInventory() {
		return this.inventory;
	}
}
