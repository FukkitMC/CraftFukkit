package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.command;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.command.CommandOutputAccess;
import net.minecraft.server.command.CommandOutput;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandOutput.class)
public interface CommandOutputMixin extends CommandOutputAccess {

}
