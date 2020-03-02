package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.entity.BlockEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DropperBlock.class)
public class DropperBlockMixin {
	// isn't this an overwrite with extra steps?
	// well yes but actually no
	// I'm overwriting the else block where inventory != null
	@Inject(method = "dispense", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_inventoryMoveEvent(World world, BlockPos pos, CallbackInfo ci, BlockPointerImpl blockPointerImpl, DispenserBlockEntity dispenserBlockEntity, int i, ItemStack itemStack, Direction direction, Inventory inventory) {
		CraftItemStack oitemstack = CraftItemStack.asCraftMirror(itemStack.copy().split(1));
		org.bukkit.inventory.Inventory destinationInventory;
		if (inventory instanceof DoubleInventory) {
			// TODO fix odd compile rror
			destinationInventory = null;
			//destinationInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((DoubleInventory) inventory);
		} else {
			destinationInventory = ((InventoryAccess)inventory).getOwner().getInventory();
		}

		InventoryMoveItemEvent event = new InventoryMoveItemEvent(((BlockEntityAccess)dispenserBlockEntity).getOwner().getInventory(), oitemstack.clone(), destinationInventory, true);
		((WorldAccess)world).getBukkitServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		ItemStack itemstack1 = HopperBlockEntity.transfer(dispenserBlockEntity, inventory, CraftItemStack.asNMSCopy(event.getItem()), direction.getOpposite());
		if (event.getItem().equals(oitemstack) && itemstack1.isEmpty()) {
			// CraftBukkit end
			itemstack1 = itemStack.copy();
			itemstack1.decrement(1);
		} else {
			itemstack1 = itemStack.copy();
		}

		dispenserBlockEntity.setInvStack(i, itemstack1);
		ci.cancel();
	}
}
