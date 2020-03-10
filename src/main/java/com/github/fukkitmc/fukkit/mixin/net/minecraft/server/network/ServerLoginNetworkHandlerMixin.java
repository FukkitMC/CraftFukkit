package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.network;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.PlayerManagerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerLoginNetworkHandlerAccess;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.net.SocketAddress;

@Implements (@Interface (iface = ServerLoginNetworkHandlerAccess.class, prefix = "fukkit$"))
@Mixin (ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
	// unsafe but probably safe
	private static final ThreadLocal<ServerPlayerEntity> SERVER_PLAYER_ENTITY = new ThreadLocal<>();
	private static final Text DUMMY_TEXT = new LiteralText("Something went wrong!");
	@Shadow @Final private static Logger LOGGER;
	public String hostName;
	@Shadow @Final public ClientConnection connection;

	@Shadow @Final private MinecraftServer server;

	@Shadow private GameProfile profile;

	public void fukkit$disconnect(String text) {
		try {
			Text ichatbasecomponent = new LiteralText(text);
			LOGGER.info("Disconnecting {}: {}", this.getConnectionInfo(), text);
			this.connection.send(new LoginDisconnectS2CPacket(ichatbasecomponent));
			this.connection.disconnect(ichatbasecomponent);
		} catch (Exception exception) {
			LOGGER.error("Error whilst disconnecting player", exception);
		}
	}

	@Shadow public abstract String getConnectionInfo();

	public MinecraftServer fukkit$getServer() {
		return this.server;
	}

	@Redirect (method = "acceptPlayer", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/server/PlayerManager;checkCanJoin" +
	                                                       "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)" +
	                                                       "Lnet/minecraft/text/Text;"))
	private Text fukkit_newIf(PlayerManager manager, SocketAddress socketAddress, GameProfile gameProfile) {
		ServerPlayerEntity entity = ((PlayerManagerAccess) this.server.getPlayerManager())
		                            .attemptLogin((ServerLoginNetworkHandler) (Object) this, this.profile, this.hostName);
		SERVER_PLAYER_ENTITY.set(entity);
		// mega brain if statement time
		return entity == null ? DUMMY_TEXT : null;
	}

	@Redirect (method = "acceptPlayer", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;" +
	                                                       "disconnect(Lnet/minecraft/text/Text;)V"))
	private void fukkit_voidCall(ServerLoginNetworkHandler networkHandler, Text reason) {}

	// two birds one stone
	@Redirect (method = "acceptPlayer", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/server/PlayerManager;createPlayer" +
	                                                       "(Lcom/mojang/authlib/GameProfile;)" +
	                                                       "Lnet/minecraft/server/network/ServerPlayerEntity;"))
	private ServerPlayerEntity fukkit_proccessLogin(PlayerManager manager, GameProfile profile) {
		ServerPlayerEntity entity = SERVER_PLAYER_ENTITY.get();
		SERVER_PLAYER_ENTITY.remove();
		return ((PlayerManagerAccess) manager).processLogin(profile, entity);
	}

}
