package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.block.BukkitChestDoubleInventory;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.container.NameableContainerFactory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Optional;

@Mixin(targets = "net.minecraft.block.ChestBlock$2")
public class ChestBlock$2Mixin {
	@Inject(method = "getFromBoth", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_customInventory(ChestBlockEntity leftChest, ChestBlockEntity rightChest, CallbackInfoReturnable<Optional<NameableContainerFactory>> cir, Inventory inventory) {
		cir.setReturnValue(Optional.of(new BukkitChestDoubleInventory(leftChest, rightChest, (net.minecraft.inventory.DoubleInventory) inventory)));
	}
}
