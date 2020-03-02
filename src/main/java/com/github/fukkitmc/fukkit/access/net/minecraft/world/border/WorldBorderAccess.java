package com.github.fukkitmc.fukkit.access.net.minecraft.world.border;

import net.minecraft.server.world.ServerWorld;

public interface WorldBorderAccess {
	ServerWorld getServerWorld();

	void setServerWorld(ServerWorld world);
}
