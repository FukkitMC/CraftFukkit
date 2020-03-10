package com.github.fukkitmc.fukkit.access.net.minecraft.server.network;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;

public interface ServerPlayerEntityAccess extends PlayerEntityAccess<CraftPlayer> {
	String getDisplayName();

	void setDisplayName(String name);

	Text getListName();

	void setListName(Text listName);

	Location getCompassTarget();

	void setCompassTarget(Location location);

	int getNewExp();

	void setNewExp(int exp);

	int getNewLevel();

	void setNewLevel(int level);

	int getNewTotalExp();

	void setNewTotalExp(int exp);

	boolean shouldKeepLevel();

	double getMaxHealthCache();

	void setMaxHealthCache(double health);

	boolean isJoining();

	boolean hasSentListPacket();

	int getClientViewDistance();

	void setClientViewDistance(int distance);

	void setKeepLevel(boolean shouldKeepLevel);

	void setIsJoining(boolean isJoining);

	void setHasSentListPacket(boolean hasSentListPacket);

	BlockPos getSpawnPoint(ServerWorld world);

	Entity changeDimension(DimensionType type, PlayerTeleportEvent.TeleportCause cause);

	int nextSyncId();

	void sendMessage(Text[] texts);

	void teleport(ServerWorld worldserver, double x, double y, double z, float pitch, float yaw,
	              org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause);

	long getTimeOffset();

	void setTimeOffset(long offset);

	boolean isTimeRelative();

	void setRelativeTime(boolean time);

	long getPlayerTime();

	WeatherType getPlayerWeather();

	void setPlayerWeather(WeatherType type);

	void setPlayerWeather(WeatherType type, boolean plugin);

	void updateWeather(float rainGradFrom, float rainGradTo, float thunderGradFrom, float thunderGradTo);

	void tickWeather();

	void resetPlayerWeather();

	void forceSetPositionRotation(double x, double y, double z, float yaw, float pitch);

	void reset();
	// TODO full impl
}
