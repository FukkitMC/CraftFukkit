package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.dispenser;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.block.DispenserBlockAccessor;
import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (BlockPlacementDispenserBehavior.class)
public class BlockPlacementDispenserBehaviorMixin {
	@Inject (method = "dispenseSilently",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/item/BlockItem;place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;"),
	         cancellable = true,
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_dispenseEvent(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir, Item item, Direction direction, BlockPos blockPos, Direction direction2) {
		Block bukkitBlock = ((WorldAccess)pointer.getWorld()).getBukkit().getBlockAt(pointer.getBlockPos().getX(), pointer.getBlockPos().getY(), pointer.getBlockPos().getZ());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(stack);

		BlockDispenseEvent event = new BlockDispenseEvent(bukkitBlock, craftItem.clone(), new org.bukkit.util.Vector(pointer.getX(), pointer.getY(), pointer.getZ()));
		if (!Constants.DISPENSER_EVENT_FIRED) {
			((WorldAccess)pointer.getWorld()).getBukkitServer().getPluginManager().callEvent(event);
		}

		if (event.isCancelled()) {
			cir.setReturnValue(stack);
			return;
		}

		if (!event.getItem().equals(craftItem)) {
			// Chain to handler for new item
			ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
			DispenserBehavior behavior = DispenserBlockAccessor.getBEHAVIORS().get(eventStack.getItem());
			if (behavior != DispenserBehavior.NOOP && behavior != this) {
				behavior.dispense(pointer, eventStack);
				cir.setReturnValue(stack);
			}
		}
	}
}
