package com.github.fukkitmc.fukkit.mixin.net.minecraft.inventory;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.CraftingInventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.DefaultedList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.*;
import java.util.ArrayList;
import java.util.List;

@Implements (@Interface (iface = CraftingInventoryAccess.class, prefix = "fukkit$"))
@Mixin (CraftingInventory.class)
public class CraftingInventoryMixin {
	public List<HumanEntity> viewers = new ArrayList<>();
	@Shadow
	@Final
	private DefaultedList<ItemStack> stacks;
	private Player holder;
	private Recipe<?> recipe;
	private Inventory resultInventory;
	private int maxStack = Constants.MAX_STACK;

	public List<ItemStack> fukkit$getContents() {
		return this.stacks;
	}

	public void fukkit$onOpen(CraftHumanEntity who) {
		this.viewers.add(who);
	}

	public void fukkit$onClose(CraftHumanEntity who) {
		this.viewers.remove(who);
	}

	public List<HumanEntity> fukkit$getViewers() {
		return this.viewers;
	}

	public InventoryHolder fukkit$getOwner() {
		return this.holder;
	}

	public void fukkit$setOwner(InventoryHolder holder) {
		if (holder instanceof Player) { this.holder = (Player) holder; } else {
			throw new IllegalArgumentException(holder + " is not a player!");
		}
	}

	public void fukkit$setMaxStackSize(int size) {
		this.maxStack = size;
		((InventoryAccess) this.resultInventory).setMaxStackSize(size);
	}

	public Location fukkit$getLocation() {
		return null;
		// todo fix
		//return this.container instanceof CraftingTableContainer ? ((BlockContextAccess)((HasBlockContextAccess)this
		// .container).getContext()).getLocation():holder.getLocation();
	}

	public Recipe<?> fukkit$getCurrentRecipe() {
		return this.recipe;
	}

	public void fukkit$setCurrentRecipe(Recipe<?> recipe) {
		this.recipe = recipe;
	}

	public InventoryType fukkit$getInvType() {
		return this.stacks.size() == 4 ? InventoryType.CRAFTING : InventoryType.WORKBENCH;
	}

	public Inventory fukkit$getResultInventory() {
		return this.resultInventory;
	}

	public void fukkit$setResultInventory(Inventory inventory) {
		this.resultInventory = inventory;
	}
}
