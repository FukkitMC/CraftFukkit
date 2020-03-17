package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.dispenser;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.block.DispenserBlockAccessor;
import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (targets = {"net/minecraft/block/dispenser/DispenserBehavior$9", "net/minecraft/block/dispenser/DispenserBehavior$10", "net/minecraft/block/dispenser/DispenserBehavior$11"})
public class DispenserBehaviorMixin$9 {
	@Inject (method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
	         at = {@At (value = "INVOKE",
	                    target = "Lnet/minecraft/entity/EntityType;spawnFromItemStack(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnType;ZZ)"
	                             + "Lnet" + "/minecraft/entity/Entity;"),
	               @At (value = "NEW",
	                    target = "net/minecraft/entity/decoration/ArmorStandEntity"), @At (value = "NEW",
	                                                                                       target = "net/minecraft/entity/FireworkEntity")
	         })
	private void fukkit_dispenseEvent(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
		World world = pointer.getWorld();
		ItemStack split = stack.split(1);
		org.bukkit.block.Block block = ((WorldAccess) world).getBukkit().getBlockAt(pointer.getBlockPos().getX(), pointer.getBlockPos().getY(), pointer.getBlockPos().getZ());
		CraftItemStack craftItem = CraftItemStack.asCraftMirror(split);

		BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(0, 0, 0));
		if (!Constants.DISPENSER_EVENT_FIRED) {
			((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
		}

		if (event.isCancelled()) {
			stack.increment(1);
			cir.setReturnValue(stack);
			return;
		}

		if (!event.getItem().equals(craftItem)) {
			stack.increment(1);
			// Chain to handler for new item
			ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
			DispenserBehavior behavior = DispenserBlockAccessor.getBEHAVIORS().get(eventStack.getItem());
			if (behavior != DispenserBehavior.NOOP && behavior != this) {
				behavior.dispense(pointer, eventStack);
				cir.setReturnValue(stack);
			}
		}
	}

	@Redirect (method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private void fukkit_handled(ItemStack stack, int amount) { }
}
