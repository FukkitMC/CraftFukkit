package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.CraftingInventoryAccess;
import com.github.fukkitmc.fukkit.util.Constants;
import com.github.fukkitmc.fukkit.util.Constructors;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin (CraftingTableContainer.class)
public abstract class CraftingTableContainerMixin extends Container implements ContainerAccess {
	private CraftInventoryView view;
	public PlayerInventory inventory;

	@Mutable @Shadow @Final private CraftingInventory craftingInv;

	@Mutable @Shadow @Final private CraftingResultInventory resultInv;

	@Shadow @Final private PlayerEntity player;

	protected CraftingTableContainerMixin(ContainerType<?> type, int syncId) {
		super(type, syncId);
	}

	@Redirect (method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/container/BlockContext;" +
	                    ")V",
	           at = @At (value = "NEW", target = "net/minecraft/inventory/CraftingInventory"))
	private CraftingInventory fukkit_switched(Container container, int width, int height) {return null;}

	@Redirect (method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/container/BlockContext;" +
	                    ")V",
	           at = @At (value = "NEW", target = "net/minecraft/inventory/CraftingResultInventory"))
	private CraftingResultInventory fukkit_switch(int syncId, PlayerInventory playerInventory,
	                                              BlockContext blockContext) {
		this.resultInv = new CraftingResultInventory();
		this.craftingInv = Constructors.newCraftingInventory(this, 3, 3, playerInventory.player);
		((CraftingInventoryAccess) this.craftingInv).setResultInventory(this.resultInv);
		this.inventory = playerInventory;
		return this.resultInv;
	}

	@Redirect (method = "method_17401", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/container/CraftingTableContainer;" +
	                                                       "updateResult(ILnet/minecraft/world/World;" +
	                                                       "Lnet/minecraft/entity/player/PlayerEntity;" +
	                                                       "Lnet/minecraft/inventory/CraftingInventory;" +
	                                                       "Lnet/minecraft/inventory/CraftingResultInventory;)V"))
	private void fukkit_passThis(int syncId, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory) {
		Constants.updateResult(this.syncId, world, this.player, this.craftingInv, this.resultInv, this);
	}


	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) this.inventory.player).getBukkit(), new CraftInventoryCrafting(this.craftingInv, this.resultInv),
		                                   this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getCheckReachable()) { cir.setReturnValue(true); }
	}


}
