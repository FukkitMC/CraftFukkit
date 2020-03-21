package com.github.fukkitmc.fukkit.access.net.minecraft.server.command;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.command.CommandSender;

public interface ServerCommandSourceAccess {
	void setCurrentCommand(CommandNode<ServerCommandSource> currentCommand);
	CommandNode<ServerCommandSource> getCurrentCommand();
	boolean hasPermission(int level, String bukkitPermission);
	CommandSender getBukkitSender();
}
