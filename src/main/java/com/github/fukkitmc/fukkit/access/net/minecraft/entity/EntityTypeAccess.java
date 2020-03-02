package com.github.fukkitmc.fukkit.access.net.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.entity.CreatureSpawnEvent;

public interface EntityTypeAccess<T extends Entity> {
	T spawnCreature(World world, CompoundTag tag, Text text, PlayerEntity player, BlockPos pos, SpawnType type, boolean alignPosition, boolean invertY, CreatureSpawnEvent.SpawnReason spawnReason);
}
