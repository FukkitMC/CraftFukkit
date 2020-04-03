package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LookTargetUtil.class)
public class LookTargetUtilMixin {
	@Inject(method = "give", at = @At("HEAD"), cancellable = true)
	private static void fukkit_noEmptyLoot(LivingEntity entity, ItemStack stack, LivingEntity target, CallbackInfo ci) {
		if(stack.isEmpty())
			ci.cancel();
	}
}
