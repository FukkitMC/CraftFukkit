package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.LoomContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.ListTag;
import org.bukkit.craftbukkit.inventory.CraftInventoryLoom;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (LoomContainer.class)
public abstract class LoomContainerMixin implements ContainerAccess {
	@Shadow @Final private Inventory inputInventory;

	@Shadow @Final private Inventory outputInventory;

	private CraftInventoryView view;
	public PlayerInventory inventory;

	@Inject (
	method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/container/BlockContext;)V",
	at = @At ("TAIL"))
	private void fukkit_initPlayer(int syncId, PlayerInventory playerInventory, BlockContext blockContext,
	                               CallbackInfo ci) {
		this.inventory = playerInventory;
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) this.inventory.player).getBukkit(), new CraftInventoryLoom(this.inputInventory, this.outputInventory),
		                                   (Container) (Object) this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.getCheckReachable()) { cir.setReturnValue(true); }
	}


	@ModifyVariable (method = "updateOutputSlot", at = @At (value = "INVOKE_ASSIGN",
	                                                        target = "Lnet/minecraft/nbt/CompoundTag;getList" +
	                                                                 "(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;",
	                                                        shift = At.Shift.AFTER, ordinal = 0),
	                 index = 7,
	                 ordinal = 0)
	private ListTag fukkit_size(ListTag listTag) {
		while (listTag.size() > 20) { listTag.remove(20); }
		return listTag;
	}


}
