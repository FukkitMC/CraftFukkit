package com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity.LecternBlockEntityAccess;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LecternInventory implements Inventory, InventoryAccess {
	public List<HumanEntity> transaction = new ArrayList<>();
	private int maxStack = 1;
	public final LecternBlockEntity blockEntity;
	public LecternInventory(LecternBlockEntity entity) {this.blockEntity = entity;}

	@Override
	public List<ItemStack> getContents() {
		return Collections.singletonList(this.blockEntity.getBook());
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
	public void setMaxStackSize(int i) {
		this.maxStack = i;
	}

	@Override
	public Location getLocation() {
		return new Location(((WorldAccess) this.blockEntity.getWorld()).getBukkit(), this.blockEntity.getPos().getX(), this.blockEntity.getPos().getY(), this.blockEntity.getPos().getZ());
	}

	@Override
	public InventoryHolder getOwner() {
		return ((InventoryAccess) this.blockEntity).getOwner();
	}
	// CraftBukkit end

	@Override
	public int getInvSize() {
		return 1;
	}

	@Override
	public boolean isInvEmpty() {
		return this.blockEntity.getBook().isEmpty();
	}

	@Override
	public ItemStack getInvStack(int i) {
		return i == 0 ? this.blockEntity.getBook() : ItemStack.EMPTY;
	}

	@Override
	public ItemStack takeInvStack(int i, int j) {
		if (i == 0) {
			ItemStack itemstack = this.blockEntity.getBook().split(j);

			if (this.blockEntity.getBook().isEmpty()) {
				((LecternBlockEntityAccess) this.blockEntity).callOnBookRemoved();
			}

			return itemstack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack removeInvStack(int i) {
		if (i == 0) {
			ItemStack itemstack = this.blockEntity.getBook();

			this.blockEntity.setBook(ItemStack.EMPTY);
			((LecternBlockEntityAccess) this.blockEntity).callOnBookRemoved();
			return itemstack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	// CraftBukkit start
	@Override
	public void setInvStack(int i, ItemStack stack) {
		if (i == 0) {
			this.blockEntity.setBook(stack);
			if (this.blockEntity.getWorld() != null) {
				LecternBlock.setHasBook(this.blockEntity.getWorld(), this.blockEntity.getPos(), this.blockEntity.getCachedState(), this.blockEntity.hasBook());
			}
		}
	}
	// CraftBukkit end

	@Override
	public int getInvMaxStackAmount() {
		return this.maxStack; // CraftBukkit
	}

	@Override
	public void markDirty() {
		this.blockEntity.markDirty();
	}

	@Override
	public boolean canPlayerUseInv(PlayerEntity playerEntity) {
		return this.blockEntity.getWorld().getBlockEntity(this.blockEntity.getPos()) == this.blockEntity && (!(playerEntity.squaredDistanceTo((double) this.blockEntity.getPos().getX() + 0.5D, (double) this.blockEntity.getPos().getY() + 0.5D, (double) this.blockEntity.getPos().getZ() + 0.5D) > 64.0D) && this.blockEntity.hasBook());
	}

	@Override
	public boolean isValidInvStack(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public void clear() {}
}