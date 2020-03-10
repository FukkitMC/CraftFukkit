package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.container.CommonContainerAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.EnchantingTableContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (EnchantingTableContainer.class)
public abstract class EnchantingTableContainerMixin extends Container implements CommonContainerAccess, BlockContextContainerAccess {
	@Shadow @Final private BlockContext context;

	protected EnchantingTableContainerMixin(ContainerType<?> type, int syncId) {
		super(type, syncId);
	}

	@Redirect(method = "onContentChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEnchantable()Z"))
	private boolean fukkit_relax(ItemStack stack) {
		return true;
	}

	@Inject(method = "method_17411", at = @At(value = "INVOKE", target = "Lnet/minecraft/container/Container;sendContentUpdates()V"), cancellable = true, locals = LocalCapture.PRINT)
	private void fukkit_prepareEvent(ItemStack stack, World world, BlockPos pos) {

	}

	@Override
	public BlockContext getContext() {
		return this.context;
	}
}
