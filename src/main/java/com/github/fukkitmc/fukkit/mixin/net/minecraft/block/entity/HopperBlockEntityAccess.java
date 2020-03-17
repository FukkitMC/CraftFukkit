package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import net.minecraft.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin (HopperBlockEntity.class)
public interface HopperBlockEntityAccess {
	@Invoker
	void callSetCooldown(int cooldown);
}
