package com.github.fukkitmc.fukkit.access.net.minecraft.server;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import com.github.fukkitmc.fukkit.annotations.BukkitAccessor;
import com.github.fukkitmc.fukkit.annotations.BukkitBridge;
import jline.console.ConsoleReader;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;

@BukkitBridge(MinecraftServer.class)
public interface MinecraftServerAccess extends CraftHandled<CraftServer> {
	@BukkitAccessor("console")
	ConsoleCommandSender getConsoleCommandSender();
	@BukkitAccessor("console")
	void setConsoleCommandSender(ConsoleCommandSender sender);
	@BukkitAccessor("reader")
	ConsoleReader getReader();
}
