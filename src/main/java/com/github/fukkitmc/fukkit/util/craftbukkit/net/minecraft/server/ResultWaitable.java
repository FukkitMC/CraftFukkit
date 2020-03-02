package com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.server;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.PlayerPreLoginEvent;

public class ResultWaitable extends Waitable<PlayerPreLoginEvent.Result> {
	private final CraftServer server;
	private final PlayerPreLoginEvent event;

	public ResultWaitable(CraftServer server, PlayerPreLoginEvent event) {
		this.server = server;
		this.event = event;
	}

	@Override
	protected PlayerPreLoginEvent.Result evaluate() {
		this.server.getPluginManager().callEvent(this.event);
		return this.event.getResult();
	}
}