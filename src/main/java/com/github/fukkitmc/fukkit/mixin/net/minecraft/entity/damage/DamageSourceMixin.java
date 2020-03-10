package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.damage;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.damage.DamageSourceAccess;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Implements (@Interface (iface = DamageSourceAccess.class, prefix = "fukkit$"))
@Mixin (DamageSource.class)
public class DamageSourceMixin {
	private boolean sweep;

	public boolean fukkit$isSweep() {
		return this.sweep;
	}

	public DamageSource fukkit$sweep() {
		this.sweep = true;
		return (DamageSource) (Object) this;
	}
}
