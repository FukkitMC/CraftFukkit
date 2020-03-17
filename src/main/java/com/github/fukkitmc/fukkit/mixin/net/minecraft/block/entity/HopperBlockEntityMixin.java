package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.entity.BlockEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.Direction;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.ArrayList;
import java.util.List;

import static com.github.fukkitmc.fukkit.util.Constants.MAX_STACK;

@Mixin (HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LockableContainerBlockEntityMixin implements Inventory {
	@Shadow private DefaultedList<ItemStack> inventory;

	@Override
	@Shadow
	public abstract ItemStack takeInvStack(int slot, int amount);

	@Override
	@Shadow
	public abstract void setInvStack(int slot, ItemStack stack);

	@Shadow
	protected abstract void setCooldown(int cooldown);

	@Shadow
	public static native ItemStack transfer(Inventory from, Inventory to, ItemStack stack, Direction side);

	public List<HumanEntity> transaction = new ArrayList<>();
	private int maxStack = MAX_STACK;
	private static ItemStack retStack;

	@Inject (method = "insert",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/block/entity/HopperBlockEntity;transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)" + "Lnet/minecraft/item" +
	                            "/ItemStack;"),
	         cancellable = true,
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_invMoveEvent0(CallbackInfoReturnable<Boolean> cir, Inventory to, Direction direction, int i, ItemStack itemStack) {
		CraftItemStack stack = CraftItemStack.asCraftMirror(this.takeInvStack(i, 1));
		org.bukkit.inventory.Inventory destinationInventory = null;
		// Have to special case large chests as they work oddly
		if (to instanceof DoubleInventory) {
			// fixme odd compile error
			//  destinationInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((DoubleInventory) to);
		} else {
			destinationInventory = ((InventoryAccess) to).getOwner().getInventory();
		}

		InventoryMoveItemEvent event = new InventoryMoveItemEvent(this.getOwner().getInventory(), stack.clone(), destinationInventory, true);
		((WorldAccess) this.getWorld()).getBukkitServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			this.setInvStack(i, itemStack);
			this.setCooldown(8); // Delay hopper checks
			cir.setReturnValue(false);
			retStack = null;
			return;
		}
		retStack = transfer(this, to, CraftItemStack.asNMSCopy(event.getItem()), direction);
	}

	@Redirect (method = "insert",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/block/entity/HopperBlockEntity;transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)" + "Lnet/minecraft/item" +
	                              "/ItemStack;"))
	private ItemStack fukkit_invMove0(Inventory from, Inventory to, ItemStack stack, Direction side) {
		return retStack;
	}

	@Inject (method = "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/block/entity/HopperBlockEntity;transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)" + "Lnet/minecraft/item" +
	                            "/ItemStack;"),
	         cancellable = true,
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private static void fukkit_invMoveEvent(Hopper hopper, Inventory inventory, int slot, Direction side, CallbackInfoReturnable<Boolean> cir, ItemStack itemStack, ItemStack itemStack2) {
		CraftItemStack stack = CraftItemStack.asCraftMirror(inventory.takeInvStack(slot, 1));

		org.bukkit.inventory.Inventory sourceInventory = null;
		// Have to special case large chests as they work oddly
		if (inventory instanceof DoubleInventory) {
			// fixme compile error
			//sourceInventory = new CraftInventoryDoubleChest((DoubleInventory) inventory);
		} else {
			sourceInventory = ((InventoryAccess)inventory).getOwner().getInventory();
		}

		InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, stack.clone(), ((InventoryAccess)hopper).getOwner().getInventory(), false);

		((WorldAccess)hopper.getWorld()).getBukkitServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			inventory.setInvStack(slot, itemStack2);

			if (hopper instanceof HopperBlockEntity) {
				((HopperBlockEntityAccess) hopper).callSetCooldown(8); // Delay hopper checks
			} else if (hopper instanceof HopperMinecartEntity) {
				((HopperMinecartEntity) hopper).setTransferCooldown(4); // Delay hopper minecart checks
			}
			cir.setReturnValue(false);
			retStack = null;
			return;
		}
		retStack = transfer(inventory, hopper, CraftItemStack.asNMSCopy(event.getItem()), null);
	}

	@Redirect (method = "extract(Lnet/minecraft/block/entity/Hopper;Lnet/minecraft/inventory/Inventory;ILnet/minecraft/util/math/Direction;)Z",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/block/entity/HopperBlockEntity;transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)" + "Lnet/minecraft/item" +
	                              "/ItemStack;"))
	private static ItemStack fukkit_invMove(Inventory from, Inventory to, ItemStack stack, Direction side) {
		return retStack;
	}

	@Inject (method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z",
	         at = @At ("HEAD"),
	         cancellable = true)
	private static void fukkit_pickupEvent(Inventory inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
		InventoryPickupItemEvent event = new InventoryPickupItemEvent(((InventoryAccess) inventory).getOwner().getInventory(), (org.bukkit.entity.Item) ((EntityAccess<?>) itemEntity).getBukkit());
		((WorldAccess) itemEntity.world).getBukkitServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
		}
	}


	@Override
	public List<ItemStack> getContents() {
		return this.inventory;
	}

	@Override
	public void onOpen(CraftHumanEntity who) {
		this.transaction.add(who);
	}

	@Override
	public void onClose(CraftHumanEntity who) {
		this.transaction.remove(who);
	}

	@Override
	public List<HumanEntity> getViewers() {
		return this.transaction;
	}

	@Override
	public void setMaxStackSize(int size) {
		this.maxStack = size;
	}
}
