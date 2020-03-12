package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.util.LocalVariable;
import com.google.common.base.Preconditions;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin (Container.class)
public abstract class ContainerMixin implements ContainerAccess {
	@Shadow private int quickCraftStage;
	@Shadow @Final public int syncId;

	@Shadow
	public abstract Slot getSlot(int index);

	public boolean checkReachable;
	private Text title;
	private LocalVariable<Map<Integer, ItemStack>> stacks = new LocalVariable<>();


	@Inject (method = "onSlotClick",
	         at = @At (value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"))
	private void fukkit_localInit(int slotId, int clickData, SlotActionType actionType, PlayerEntity playerEntity,
	                              CallbackInfoReturnable<ItemStack> cir) {
		this.stacks.push(new HashMap<>());
	}

	// slice because big ass decomp error
	@Redirect (method = "onSlotClick",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/container/Slot;setStack(Lnet/minecraft/item/ItemStack;)V", ordinal = 0),
	           slice = @Slice (from = @At (value = "INVOKE",
	                                       target = "Lnet/minecraft/container/Container;calculateStackSize" +
	                                                "(Ljava/util/Set;ILnet/minecraft/item/ItemStack;I)V")))
	private void fukkit_putInMap(Slot slot, ItemStack itemStack) {
		this.stacks.get().put(slot.id, itemStack);
	}

	@Redirect (method = "onSlotClick",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCount(I)V", ordinal = 1),
	           slice = @Slice (from = @At (value = "INVOKE",
	                                       target = "Lnet/minecraft/container/Container;calculateStackSize" +
	                                                "(Ljava/util/Set;ILnet/minecraft/item/ItemStack;I)V"),
	                           to = @At (value = "INVOKE:FIRST",
	                                     target = "Lnet/minecraft/entity/player/PlayerInventory;setCursorStack" +
	                                              "(Lnet/minecraft/item/ItemStack;)V")))
	private void fukkit_dragEvent(ItemStack itemStack2, int count, int slotId, int clickData,
	                              SlotActionType actionType,
	                              PlayerEntity playerEntity) {
		Map<Integer, ItemStack> draggedSlots = this.stacks.pop();
		PlayerInventory playerInventory = playerEntity.inventory;
		// CraftBukkit start - InventoryDragEvent
		InventoryView view = this.getBukkitView();
		org.bukkit.inventory.ItemStack newcursor = CraftItemStack.asCraftMirror(itemStack2);
		newcursor.setAmount(count);
		Map<Integer, org.bukkit.inventory.ItemStack> eventmap = new HashMap<Integer, org.bukkit.inventory.ItemStack>();
		for (Map.Entry<Integer, ItemStack> ditem : draggedSlots.entrySet()) {
			eventmap.put(ditem.getKey(), CraftItemStack.asBukkitCopy(ditem.getValue()));
		}

		// It's essential that we set the cursor to the new value here to prevent item duplication if a plugin closes
		// the inventory.
		ItemStack oldCursor = playerInventory.getCursorStack();
		playerInventory.setCursorStack(CraftItemStack.asNMSCopy(newcursor));

		InventoryDragEvent event = new InventoryDragEvent(view, (newcursor.getType() != org.bukkit.Material.AIR ?
		                                                         newcursor : null), CraftItemStack
		                                                                            .asBukkitCopy(oldCursor),
		                                                  this.quickCraftStage == 1, eventmap);
		((WorldAccess) playerEntity.world).getBukkitServer().getPluginManager().callEvent(event);

		// Whether or not a change was made to the inventory that requires an update.
		boolean needsUpdate = event.getResult() != Event.Result.DEFAULT;

		if (event.getResult() != Event.Result.DENY) {
			for (Map.Entry<Integer, ItemStack> dslot : draggedSlots.entrySet()) {
				view.setItem(dslot.getKey(), CraftItemStack.asBukkitCopy(dslot.getValue()));
			}
			// The only time the carried item will be set to null is if the inventory is closed by the server.
			// If the inventory is closed by the server, then the cursor items are dropped.  This is why we change the
			// cursor early.
			if (playerInventory.getCursorStack() != null) {
				playerInventory.setCursorStack(CraftItemStack.asNMSCopy(event.getCursor()));
				needsUpdate = true;
			}
		} else {
			playerInventory.setCursorStack(oldCursor);
		}

		if (needsUpdate && playerEntity instanceof ServerPlayerEntity) {
			((ServerPlayerEntity) playerEntity).openContainer((Container) (Object) this);
		}
	}

	@Redirect (method = "onSlotClick",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/entity/player/PlayerInventory;setCursorStack" +
	                              "(Lnet/minecraft/item/ItemStack;)V",
	                     ordinal = 0),
	           slice = @Slice (from = @At (value = "INVOKE",
	                                       target = "Lnet/minecraft/container/Container;calculateStackSize" +
	                                                "(Ljava/util/Set;ILnet/minecraft/item/ItemStack;I)V")))
	private void fukkit_voidCall(PlayerInventory inventory, ItemStack stack) {}

	@Redirect (method = "onSlotClick", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem" +
	                                                      "(Lnet/minecraft/item/ItemStack;Z)" +
	                                                      "Lnet/minecraft/entity/ItemEntity;", ordinal = 0),
	           slice = @Slice (from = @At (value = "CONSTANT", args = "intValue=-999")))
	private ItemEntity fukkit_dropItem(PlayerEntity entity, ItemStack stack, boolean bl) {
		return null;
	}

	@Redirect (method = "onSlotClick", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/entity/player/PlayerInventory;" +
	                                                      "setCursorStack" +
	                                                      "(Lnet/minecraft/item/ItemStack;)V",
	                                             ordinal = 0),
	           slice = @Slice (from = @At (value = "CONSTANT", args = "intValue=-999")))
	private void fukkit_dropItem(PlayerInventory inventory, ItemStack stack, int slotId, int clickData,
	                             SlotActionType actionType, PlayerEntity playerEntity) {
		ItemStack carried = inventory.getCursorStack();
		inventory.setCursorStack(stack);
		playerEntity.dropItem(carried, true);
	}

	@Inject (method = "onSlotClick",
	         at = @At (value = "INVOKE", target = "Lnet/minecraft/container/Slot;markDirty()V", shift =
	                                                                                            At.Shift.AFTER),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_ensureClient(int slotId, int clickData, SlotActionType actionType, PlayerEntity playerEntity,
	                                 CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack,
	                                 PlayerInventory playerInventory, Slot slot4, ItemStack itemStack7,
	                                 ItemStack itemStack8, int p) {
		if (playerEntity instanceof ServerPlayerEntity && slot4.getMaxStackAmount() != 64) {
			((ServerPlayerEntity) playerEntity).networkHandler
			.sendPacket(new ContainerSlotUpdateS2CPacket(this.syncId, slot4.id, slot4.getStack()));
			// Updating a crafting inventory makes the client reset the result slot, have to send it again
			if (this.getBukkitView()
			        .getType() == InventoryType.WORKBENCH || this
			                                                                     .getBukkitView()
			                                                                     .getType() == InventoryType.CRAFTING) {
				((ServerPlayerEntity) playerEntity).networkHandler
				.sendPacket(new ContainerSlotUpdateS2CPacket(this.syncId, 0, this.getSlot(0).getStack()));
			}
		}
	}

	@Redirect (method = "close", at = @At (value = "INVOKE",
	                                       target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem" +
	                                                "(Lnet/minecraft/item/ItemStack;Z)" +
	                                                "Lnet/minecraft/entity/ItemEntity;"))
	private ItemEntity fukkit_voidCall(PlayerEntity entity, ItemStack stack, boolean bl) {
		return null;
	}

	@Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setCursorStack(Lnet/minecraft/item/ItemStack;)V"))
	private void fukkit_spigot4556(PlayerInventory inventory, ItemStack stack, PlayerEntity player) {
		ItemStack cursor = inventory.getCursorStack();
		inventory.setCursorStack(ItemStack.EMPTY);
		player.dropItem(cursor, false);
	}

	@Override
	public boolean getCheckReachable() {
		return this.checkReachable;
	}

	@Override
	public void setCheckReachable(boolean check) {
		this.checkReachable = check;
	}

	@Override
	public Text getTitle() {
		Preconditions.checkState(this.title != null, "Title not set");
		return this.title;
	}

	@Override
	public void setTitle(Text text) {
		Preconditions.checkState(this.title == null, "Title already set");
		this.title = text;
	}
}
