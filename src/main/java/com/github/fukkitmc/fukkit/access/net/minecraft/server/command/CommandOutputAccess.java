package com.github.fukkitmc.fukkit.access.net.minecraft.server.command;

import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.command.CommandSender;

public interface CommandOutputAccess {
	CommandSender getBukkitSender(ServerCommandSource wrapper);
}
