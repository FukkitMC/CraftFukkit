package com.github.fukkitmc.fukkit.access.net.minecraft.server.world;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

public interface ServerWorldAccess extends WorldAccess {
	@Override
	BlockEntity getBlockEntity(BlockPos pos, boolean validate);

	BlockEntity fixBlockEntity(BlockPos pos, Block type, BlockEntity found);

	boolean spawnEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason);

	boolean addEntitySerialized(Entity entity, CreatureSpawnEvent.SpawnReason reason);

	boolean addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason);

	void strikeLightning(LightningEntity entitylightning, LightningStrikeEvent.Cause cause);

	<T extends ParticleEffect> int spawnParticles(ServerPlayerEntity entity, T particle, double x, double y, double z,
	                                              int count, double deltaX, double deltaY, double deltaZ, double speed
	, boolean force);

}
