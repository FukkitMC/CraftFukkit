package com.github.fukkitmc.fukkit.access.net.minecraft.server.network;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import java.util.Set;

public interface ServerPlayNetworkHandlerAccess {
	boolean hasProcessedDisconnect();
	void setProcessedDisconnect(boolean processed);
	CraftPlayer getPlayer();
	void disconnect(String string);
	void teleportRequest(double x, double y, double z, float yaw, float pitch, Set<PlayerPositionLookS2CPacket.Flag> set, PlayerTeleportEvent.TeleportCause cause);
	void teleportRequest(double x, double y, double z, float yaw, float pitch, PlayerTeleportEvent.TeleportCause cause);
	void teleport(Location dest);
	void chat(String msg, boolean async);
	boolean isDisconnected();
}
