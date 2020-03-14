package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.container.AbstractFurnaceContainer;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.PropertyDelegate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import org.bukkit.craftbukkit.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceContainer.class)
public abstract class AbstractFurnaceContainerMixin implements ContainerAccess {
	@Shadow @Final private Inventory inventory;

	private CraftInventoryView view;
	public PlayerInventory playerInventory;

	@Inject (
	method = "<init>(Lnet/minecraft/container/ContainerType;Lnet/minecraft/recipe/RecipeType;" +
	         "ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;" +
	         "Lnet/minecraft/container/PropertyDelegate;)V",
	at = @At ("TAIL"))
	private void fukkit_initPlayer(ContainerType<?> containerType,
	                               RecipeType<? extends AbstractCookingRecipe> recipeType, int syncId,
	                               PlayerInventory playerInventory, Inventory inventory,
	                               PropertyDelegate propertyDelegate, CallbackInfo ci) {
		this.playerInventory = playerInventory;
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) playerInventory.player).getBukkit(), new CraftInventoryFurnace(((AbstractFurnaceBlockEntity) this.inventory)),
		                                   (Container) (Object) this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getCheckReachable()) { cir.setReturnValue(true); }
	}
}
