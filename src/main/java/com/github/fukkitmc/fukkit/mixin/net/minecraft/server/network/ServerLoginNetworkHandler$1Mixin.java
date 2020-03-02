package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.network;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.MinecraftServerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerLoginNetworkHandlerAccess;
import com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.server.ResultWaitable;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

@Mixin (targets = "net/minecraft/server/network/ServerLoginNetworkHandler$1")
public class ServerLoginNetworkHandler$1Mixin extends Thread {
	@SuppressWarnings ("ShadowTarget")
	@Shadow
	private ServerLoginNetworkHandler field_14176;

	@Inject (method = "run", at = @At (value = "JUMP", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void fukkit_preLoginEvent(CallbackInfo ci, GameProfile profile, String string) {
		try {
			if (!this.field_14176.connection.isOpen()) {
				ci.cancel();
				return;
			}
			String name = profile.getName();
			InetAddress address = ((InetSocketAddress) this.field_14176.connection.getAddress()).getAddress();
			UUID uuid = profile.getId();
			CraftServer server = ((MinecraftServerAccess) ((ServerLoginNetworkHandlerAccess) this.field_14176).getServer()).getBukkit();
			AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(name, address, uuid);
			server.getPluginManager().callEvent(asyncEvent);
			if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
				final PlayerPreLoginEvent event = new PlayerPreLoginEvent(name, address, uuid);
				if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
					event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
				}

				Waitable<PlayerPreLoginEvent.Result> waitable = new ResultWaitable(server, event);
				((ServerLoginNetworkHandlerAccess) this.field_14176).getServer().addServerGuiTickable(waitable);
				if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
					((ServerLoginNetworkHandlerAccess) this.field_14176).disconnect(event.getKickMessage());
					ci.cancel();
				}
			} else {
				if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
					((ServerLoginNetworkHandlerAccess) this.field_14176).disconnect(asyncEvent.getKickMessage());
					ci.cancel();
				}
			}
		} catch (Exception e) {
			((ServerLoginNetworkHandlerAccess) this.field_14176).disconnect("Failed to verify username!");
			((MinecraftServerAccess)((ServerLoginNetworkHandlerAccess)this.field_14176).getServer()).getBukkit().getLogger().log(java.util.logging.Level.WARNING, "Exception verifying " + profile.getName(), e);
		}
	}
}
