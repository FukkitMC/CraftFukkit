package com.github.fukkitmc.fukkit.access.net.minecraft.server;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;

public interface MinecraftServerAccess extends CraftHandled<CraftServer> {
	ConsoleCommandSender getConsoleCommandSender();

	void setConsoleCommandSender(ConsoleCommandSender sender);

	ConsoleReader getReader();
}
