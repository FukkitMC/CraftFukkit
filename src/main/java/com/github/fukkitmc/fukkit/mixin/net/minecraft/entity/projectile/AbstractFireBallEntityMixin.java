package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.projectile;

import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractFireballEntity.class)
public abstract class AbstractFireBallEntityMixin {
	@Shadow public abstract void setItem(ItemStack stack);

	@Redirect(method = "readCustomDataFromTag", at = @At(target = "Lnet/minecraft/entity/projectile/AbstractFireballEntity;setItem(Lnet/minecraft/item/ItemStack;)V", value = "INVOKE"))
	public void redirect(AbstractFireballEntity entity, ItemStack stack) {
		if(!stack.isEmpty()) this.setItem(stack);
	}
}
