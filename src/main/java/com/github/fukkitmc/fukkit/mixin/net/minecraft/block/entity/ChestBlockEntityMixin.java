package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

import static com.github.fukkitmc.fukkit.util.Constants.MAX_STACK;

@Mixin (ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin extends BlockEntity implements InventoryAccess {
	@Shadow private DefaultedList<ItemStack> inventory;
	@Shadow protected int viewerCount;
	public List<HumanEntity> transaction = new ArrayList<>();
	private int maxStack = MAX_STACK;

	public ChestBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject (method = {"onInvOpen", "onInvClose"},
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/block/entity/ChestBlockEntity;onInvOpenOrClose()V"))
	private void fukkit_redstoneEvent(PlayerEntity player, CallbackInfo ci) {
		if (this.world == null) ci.cancel();
		if (this.getCachedState().getBlock() == Blocks.TRAPPED_CHEST) {
			int oldPower = Math.max(0, Math.min(15, this.viewerCount - 1));
			int newPower = Math.max(0, Math.min(15, this.viewerCount));
			if (oldPower != newPower) CraftEventFactory.callRedstoneChange(this.world, this.pos, oldPower, newPower);
		}
	}

	@Override
	public boolean shouldNotCopyTagFromItem() {
		return true;
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
