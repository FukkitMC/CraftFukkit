package com.github.fukkitmc.fukkit.access.net.minecraft.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.bukkit.Location;

public interface PlayerManagerAccess {
	String disconnect(ServerPlayerEntity entity);
	ServerPlayerEntity attemptLogin(ServerLoginNetworkHandler networkHandler, GameProfile profile, String host);
	ServerPlayerEntity processLogin(GameProfile profile, ServerPlayerEntity entity);
	ServerPlayerEntity moveToWorld(ServerPlayerEntity entityplayer, DimensionType dimensionmanager, boolean alive, Location location, boolean avoidSuffocation);
	void sendAll(Packet<?> packet, PlayerEntity entity);
	void sendAll(Packet packet, World world);
	void sendMessage(Text[] lines);
}
