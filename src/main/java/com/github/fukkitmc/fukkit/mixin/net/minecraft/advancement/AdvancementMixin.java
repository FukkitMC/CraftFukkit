package com.github.fukkitmc.fukkit.mixin.net.minecraft.advancement;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import net.minecraft.advancement.Advancement;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin (Advancement.class)
public abstract class AdvancementMixin implements CraftHandled<org.bukkit.advancement.Advancement> {
	private org.bukkit.advancement.Advancement advancement = new CraftAdvancement((Advancement) (Object) this);

	@Override
	public org.bukkit.advancement.Advancement getBukkit() {
		return this.advancement;
	}

	@Override
	public void setBukkit(org.bukkit.advancement.Advancement object) {
		this.advancement = object;
	}
}
