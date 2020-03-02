package com.github.fukkitmc.fukkit.access.net.minecraft.entity;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.ServerCommandSender;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.projectiles.ProjectileSource;

public interface EntityAccess<E extends CraftEntity> extends CraftHandled<E> {
	float getYaw();
	boolean willPersist();
	void shouldPersist(boolean persist);
	boolean isValid();
	void setValid(boolean isValid);
	ProjectileSource getProjectileSource();
	void setProjectileSource(ProjectileSource source);
	boolean shouldForceExplosionKnockback();
	void setForceExplosionKnockback(boolean knockback);
	boolean isChunkLoaded();
	default CommandSender getBukkitSender(ServerCommandSource wrapper) {
		return this.getBukkit();
	}
	void setOnFireFor(int seconds, boolean callEvent);
	void postTick();
	int getRidingCooldown();
	void setRidingCooldown(int cooldown);
	Entity teleportTo(DimensionType type, BlockPos pos);
}
