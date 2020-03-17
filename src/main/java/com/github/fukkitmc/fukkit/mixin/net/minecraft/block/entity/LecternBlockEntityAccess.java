package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import net.minecraft.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin (LecternBlockEntity.class)
public interface LecternBlockEntityAccess {
	@Invoker
	void callOnBookRemoved();
}
