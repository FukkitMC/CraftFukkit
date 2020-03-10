package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (JukeboxBlock.class)
public class JukeboxBlockMixin {
	// not vanilla?
	@Redirect (method = "setRecord", at = @At (value = "INVOKE",
	                                           target = "Lnet/minecraft/block/entity/JukeboxBlockEntity;setRecord" +
	                                                    "(Lnet/minecraft/item/ItemStack;)V"))
	private void fukkit_copy(JukeboxBlockEntity entity, ItemStack itemStack) {
		if (!itemStack.isEmpty()) { itemStack.setCount(1); }
		entity.setRecord(itemStack);
	}
}
