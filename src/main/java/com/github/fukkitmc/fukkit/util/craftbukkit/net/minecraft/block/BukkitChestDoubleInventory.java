package com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.block;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.container.Container;
import net.minecraft.container.GenericContainer;
import net.minecraft.container.NameableContainerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

// w:arr:ning
// BlockChest.DoubleInventory
public class BukkitChestDoubleInventory implements NameableContainerFactory {
	public final net.minecraft.inventory.DoubleInventory inventory;
	private final ChestBlockEntity leftChest;
	private final ChestBlockEntity rightChest;

	public BukkitChestDoubleInventory(ChestBlockEntity leftChest, ChestBlockEntity rightChest,
	                                  net.minecraft.inventory.DoubleInventory inventory) {
		this.leftChest = leftChest;
		this.rightChest = rightChest;
		this.inventory = inventory;
	}

	@Override
	public Text getDisplayName() {
		return this.leftChest.hasCustomName() ? this.leftChest.getDisplayName() :
		       (this.rightChest.hasCustomName() ? this.rightChest.getDisplayName() :
		        new TranslatableText("container.chestDouble"));
	}

	@Override
	public Container createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		if (this.leftChest.checkUnlocked(player) && this.rightChest.checkUnlocked(player)) {
			this.leftChest.checkLootInteraction(inv.player);
			this.rightChest.checkLootInteraction(inv.player);
			return GenericContainer.createGeneric9x6(syncId, inv, this.inventory);
		} else {
			return null;
		}
	}
}
