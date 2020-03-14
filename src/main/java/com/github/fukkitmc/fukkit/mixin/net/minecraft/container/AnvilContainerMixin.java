package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin (AnvilContainer.class)
public class AnvilContainerMixin extends ContainerMixin implements ContainerAccess {

	@Shadow @Final private BlockContext context;

	@Shadow @Final private Inventory inventory;

	@Shadow @Final private Inventory result;

	public int maximumRepairCost = 40;

	private CraftInventoryView view;
	public PlayerInventory playerInventory;


	@Redirect (method = "updateResult", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/inventory/Inventory;setInvStack" +
	                                                       "(ILnet/minecraft/item/ItemStack;)V"))
	private void fukkit_prepareAnvilEvent(Inventory inventory, int slot, ItemStack stack) {
		CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), ItemStack.EMPTY);
	}

	@ModifyConstant (method = "updateResult", constant = @Constant (intValue = 40), slice = @Slice (from = @At (value = "INVOKE", target = "Lorg/apache/commons/lang3/StringUtils;isBlank(Ljava/lang/CharSequence;)Z")))
	private int fukkit_repair(int maximumRepairCost) {
		return this.maximumRepairCost;
	}

	@Override public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) this.playerInventory.player).getBukkit(), new CraftInventoryAnvil(((BlockContextAccess) this.context)
		                                                                                                                              .getLocation(), this.inventory, this.result, (AnvilContainer) (Object) this),
		                                   (Container) (Object) this);
		return this.view;
	}
}
