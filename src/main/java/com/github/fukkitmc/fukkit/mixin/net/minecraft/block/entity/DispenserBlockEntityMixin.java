package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import java.util.ArrayList;
import java.util.List;

import static com.github.fukkitmc.fukkit.util.Constants.MAX_STACK;

@Mixin(DispenserBlockEntity.class)
public class DispenserBlockEntityMixin extends LockableContainerBlockEntityMixin implements InventoryAccess {
	@Shadow private DefaultedList<ItemStack> inventory;
	public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = MAX_STACK;

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
