package com.github.fukkitmc.fukkit.access.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.text.Text;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public interface ContainerAccess {
	InventoryView getBukkitView();

	boolean getReachable();

	void setReachable(boolean reachable);

	default void transferTo(Container other, org.bukkit.craftbukkit.entity.CraftHumanEntity player) {
		InventoryView source = this.getBukkitView(), destination = ((ContainerAccess) other).getBukkitView();
		((InventoryAccess) ((CraftInventory) source.getTopInventory()).getInventory()).onClose(player);
		((InventoryAccess) ((CraftInventory) source.getBottomInventory()).getInventory()).onClose(player);
		((InventoryAccess) ((CraftInventory) destination.getTopInventory()).getInventory()).onOpen(player);
		((InventoryAccess) ((CraftInventory) destination.getBottomInventory()).getInventory()).onOpen(player);
	}

	Text getTitle();

	void setTitle(Text text);

	Player getPlayer();
}
