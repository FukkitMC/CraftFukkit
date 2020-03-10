package com.github.fukkitmc.fukkit.mixin.net.minecraft.world;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.ChunkRegionAccess;
import net.minecraft.entity.Entity;
import net.minecraft.world.ChunkRegion;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Implements (@Interface (iface = ChunkRegionAccess.class, prefix = "fukkit$"))
@Mixin (ChunkRegion.class)
public abstract class ChunkRegionMixin {
	public boolean fukkit$addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
		return this
		       .spawnEntity(entity); // in craftbukkit, this parameter doesn't actually do anything, quite strange :p
	}

	@Shadow public abstract boolean spawnEntity(Entity entity);
}
