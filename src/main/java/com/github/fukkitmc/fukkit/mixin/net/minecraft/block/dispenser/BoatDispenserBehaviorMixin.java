package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.dispenser;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.block.DispenserBlockAccessor;
import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (BoatDispenserBehavior.class)
public class BoatDispenserBehaviorMixin {
	private BoatEntity boat;

	@Inject (method = "dispenseSilently",
	         at = @At (value = "NEW",
	                   target = "net/minecraft/entity/vehicle/BoatEntity"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_blockDispenseEvent(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir, Direction direction, World world, double d, double e, double f, double i) {
		ItemStack split = stack.split(1);
		Block block = ((WorldAccess)pointer.getWorld()).getBukkit().getBlockAt(pointer.getBlockPos().getX(), pointer.getBlockPos().getY(), pointer.getBlockPos().getZ());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(split);

		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(d, e + f, i));
		if (!Constants.DISPENSER_EVENT_FIRED) {
			((WorldAccess)pointer.getWorld()).getBukkitServer().getPluginManager().callEvent(event);
		}

		if (event.isCancelled()) {
			stack.increment(1);
			cir.setReturnValue(stack);
		}

		if (!event.getItem().equals(craftItem)) {
			stack.increment(1);
			// Chain to handler for new item
			ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
			DispenserBehavior dispenserBehavior = DispenserBlockAccessor.getBEHAVIORS().get(eventStack.getItem());
			if (dispenserBehavior != DispenserBehavior.NOOP && dispenserBehavior != this) {
				dispenserBehavior.dispense(pointer, eventStack);
				cir.setReturnValue(stack);
				return;
			}
		}
		this.boat = new BoatEntity(world, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
	}
	@Redirect (method = "dispenseSilently",
	           at = @At (value = "NEW",
	                   target = "net/minecraft/entity/vehicle/BoatEntity"))
	private BoatEntity fukkit_newBlock(World world, double x, double y, double z) {
		return this.boat;
	}

	@Redirect(method = "dispenseSilently", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_revert(World world, Entity entity, BlockPointer pointer, ItemStack stack) {
		if(!world.spawnEntity(entity)) {
			stack.increment(1);
		}
		return true;
	}

	@Redirect(method = "dispenseSilently", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private void fukkit_handled(ItemStack stack, int amount) {}
}
