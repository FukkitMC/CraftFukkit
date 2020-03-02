package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.mob;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.mob.MobEntityAccess;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobEntity.class)
@Implements(@Interface(iface = MobEntityAccess.class, prefix = "fukkit$"))
public class MobEntityMixin {
	// TODO skeleton impl
	@Shadow private boolean persistent;

	public void fukkit$setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
}
