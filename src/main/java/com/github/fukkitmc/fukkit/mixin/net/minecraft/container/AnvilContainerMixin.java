package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.BlockContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryAnvil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin (AnvilContainer.class)
public abstract class AnvilContainerMixin implements CommonContainerAccess {

	@Shadow @Final private BlockContext context;

	@Shadow @Final private Inventory inventory;

	@Shadow @Final private Inventory result;

	public int maximumRepairCost = 40;

	@Override
	public CraftInventory createInventory() {
		return new CraftInventoryAnvil(((BlockContextAccess) this.context)
		                               .getLocation(), this.inventory, this.result, (AnvilContainer) (Object) this);
	}

	@Redirect (method = "updateResult", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/inventory/Inventory;setInvStack" +
	                                                       "(ILnet/minecraft/item/ItemStack;)V"))
	private void fukkit_prepareAnvilEvent(Inventory inventory, int slot, ItemStack stack) {
		CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), ItemStack.EMPTY);
	}

	@ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40), slice = @Slice(from = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/StringUtils;isBlank(Ljava/lang/CharSequence;)Z")))
	private int fukkit_repair() {
		return this.maximumRepairCost;
	}
}
