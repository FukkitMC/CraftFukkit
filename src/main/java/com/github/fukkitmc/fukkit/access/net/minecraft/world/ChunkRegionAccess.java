package com.github.fukkitmc.fukkit.access.net.minecraft.world;

import net.minecraft.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public interface ChunkRegionAccess {
	boolean addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason);
}
