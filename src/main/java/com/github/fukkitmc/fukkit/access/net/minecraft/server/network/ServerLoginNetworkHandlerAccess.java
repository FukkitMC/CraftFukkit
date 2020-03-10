package com.github.fukkitmc.fukkit.access.net.minecraft.server.network;

import net.minecraft.server.MinecraftServer;

public interface ServerLoginNetworkHandlerAccess {
	@Deprecated
	void disconnect(String text);

	MinecraftServer getServer();

	String getHostName();

	void setHostName(String name);
}
