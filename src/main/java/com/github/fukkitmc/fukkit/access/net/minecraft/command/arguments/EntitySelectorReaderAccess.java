package com.github.fukkitmc.fukkit.access.net.minecraft.command.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;

public interface EntitySelectorReaderAccess {
	EntitySelector parse(boolean overridePermissions) throws CommandSyntaxException;
}
