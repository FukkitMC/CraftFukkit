package com.github.fukkitmc.fukkit.access.net.minecraft.entity.damage;

import net.minecraft.entity.damage.DamageSource;

public interface DamageSourceAccess {
	boolean isSweep();

	DamageSource sweep();
}
