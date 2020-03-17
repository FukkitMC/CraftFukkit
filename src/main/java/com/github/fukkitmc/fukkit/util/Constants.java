package com.github.fukkitmc.fukkit.util;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import java.util.Optional;

/**
 * static methods an
 */
public class Constants {
	public static final int MAX_STACK = 64;
	// MinecraftServer#currentTick
	public static int currentTick = (int) (System.currentTimeMillis() / 50);
	// SaplingBlock#treeType
	public static TreeType saplingBlockTreeType;
	/**
	 * @see com.github.fukkitmc.fukkit.mixin.net.minecraft.block.DispenserBlockMixin
	 */
	public static boolean DISPENSER_EVENT_FIRED = false;

	// CraftingTableContainer#a
	public static void updateResult(int syncId, World world, PlayerEntity player,
	                                CraftingInventory craftingInventory,
	                                CraftingResultInventory resultInventory, Container container) {
		if (!world.isClient) {
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
			ItemStack itemStack = ItemStack.EMPTY;
			Optional<CraftingRecipe> optional = world.getServer().getRecipeManager()
			                                         .getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
			if (optional.isPresent()) {
				CraftingRecipe craftingRecipe = optional.get();
				if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe)) {
					itemStack = craftingRecipe.craft(craftingInventory);
				}
			}
			itemStack = CraftEventFactory
			            .callPreCraftEvent(craftingInventory, resultInventory, itemStack, ((ContainerAccess) container)
			                                                                              .getBukkitView(), false);
			resultInventory.setInvStack(0, itemStack);
			serverPlayerEntity.networkHandler.sendPacket(new ContainerSlotUpdateS2CPacket(syncId, 0, itemStack));
		}
	}
}
