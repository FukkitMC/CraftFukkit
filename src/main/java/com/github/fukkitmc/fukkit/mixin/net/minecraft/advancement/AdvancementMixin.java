package com.github.fukkitmc.fukkit.mixin.net.minecraft.advancement;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import net.minecraft.advancement.Advancement;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin (Advancement.class)
@Implements (@Interface (iface = CraftHandled.class, prefix = "fukkit$"))
public abstract class AdvancementMixin {
	private org.bukkit.advancement.Advancement advancement = new CraftAdvancement((Advancement) (Object) this);

	public Object fukkit$getBukkit() {
		return this.advancement;
	}

	public void fukkit$setBukkit(Object object) {
		this.advancement = (org.bukkit.advancement.Advancement) object;
	}
}
