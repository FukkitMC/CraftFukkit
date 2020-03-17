package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.command;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.command.CommandOutputAccess;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/server/command/CommandOutput$1")
public class CommandOutput$1Mixin implements CommandOutputAccess {

	@Override
	public CommandSender getBukkitSender(ServerCommandSource wrapper) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
