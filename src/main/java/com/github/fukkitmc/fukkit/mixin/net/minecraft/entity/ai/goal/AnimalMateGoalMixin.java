package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.goal.AnimalMateGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (AnimalMateGoal.class)
public class AnimalMateGoalMixin {
	@Inject (method = "breed",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/entity/passive/AnimalEntity;getLovingPlayer()Lnet/minecraft/server/network/ServerPlayerEntity;"),
	         locals = LocalCapture.PRINT)
	private void fukkit_tamableEntity(CallbackInfo ci) {

	}
}
