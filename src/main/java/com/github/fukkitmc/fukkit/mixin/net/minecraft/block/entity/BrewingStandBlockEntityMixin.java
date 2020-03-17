package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.ArrayList;
import java.util.List;

@Mixin (BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin extends LockableContainerBlockEntityMixin implements InventoryAccess {
	@Shadow private DefaultedList<ItemStack> inventory;
	@Shadow private int fuel;
	private int lastTick = Constants.currentTick;
	public List<HumanEntity> transaction = new ArrayList<>();
	private int maxStack = 64;

	@Inject (method = "tick",
	         at = @At (value = "CONSTANT",
	                   args = "intValue=20"),
	         locals = LocalCapture.CAPTURE_FAILHARD,
	         cancellable = true)
	private void fukkit_brewEvent(CallbackInfo ci, ItemStack stack) {
		BrewingStandFuelEvent event = new BrewingStandFuelEvent(((WorldAccess) this.world).getBukkit().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()), CraftItemStack.asCraftMirror(stack), 20);
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			ci.cancel();
			return;
		}

		this.fuel = event.getFuelPower();
		if (this.fuel > 0 && event.isConsuming()) {
			stack.decrement(1);
		}
	}

	@Redirect (method = "tick",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private void fukkit_isConsuming(ItemStack stack, int amount) {}


	// todo not vanilla patch option, use wall time instead of tick time, the mixin is slightly complex, but it's non-vanilla behavior
	// anyways, so I don't care.


	@Inject (method = "craft",
	         at = @At ("HEAD"),
	         cancellable = true)
	private void fukkit_brewEvent(CallbackInfo ci) {
		InventoryHolder owner = this.getOwner();
		if (owner != null) {
			BrewEvent event = new BrewEvent(((WorldAccess) this.world).getBukkit().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()), (org.bukkit.inventory.BrewerInventory) owner.getInventory(), this.fuel);
			org.bukkit.Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				ci.cancel();
			}
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
