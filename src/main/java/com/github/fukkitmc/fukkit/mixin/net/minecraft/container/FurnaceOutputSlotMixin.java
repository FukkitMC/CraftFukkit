package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.entity.AbstractFurnaceBlockEntityAccess;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.container.FurnaceOutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FurnaceOutputSlot.class)
public class FurnaceOutputSlotMixin {
	@Shadow private int amount;

	@Redirect(method = "onCrafted(Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;dropExperience(Lnet/minecraft/entity/player/PlayerEntity;)V"))
	private void fukkit_newMethod(AbstractFurnaceBlockEntity entity, PlayerEntity player, ItemStack stack) {
		((AbstractFurnaceBlockEntityAccess)entity).dropExperience(player, stack, this.amount);
	}
}
