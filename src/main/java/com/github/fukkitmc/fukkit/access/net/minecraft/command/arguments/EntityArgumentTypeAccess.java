package com.github.fukkitmc.fukkit.access.net.minecraft.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;

public interface EntityArgumentTypeAccess {
	EntitySelector parse(StringReader reader, boolean overridePermissions) throws CommandSyntaxException;
}
