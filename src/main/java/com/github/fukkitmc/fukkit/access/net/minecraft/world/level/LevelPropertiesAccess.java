package com.github.fukkitmc.fukkit.access.net.minecraft.world.level;

import net.minecraft.server.world.ServerWorld;

public interface LevelPropertiesAccess {
	void checkName(String name);
	ServerWorld getServerWorld();
	void setServerWorld(ServerWorld world);
}
