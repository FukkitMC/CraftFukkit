package com.github.fukkitmc.fukkit.mixin.net.minecraft.command.arguments;

import com.github.fukkitmc.fukkit.access.net.minecraft.command.arguments.EntityArgumentTypeAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.command.arguments.EntitySelectorReaderAccess;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.arguments.EntityArgumentType;
import org.spongepowered.asm.mixin.*;

import static net.minecraft.command.arguments.EntityArgumentType.*;

@Implements (@Interface (iface = EntityArgumentTypeAccess.class, prefix = "fukkit$"))
@Mixin (EntityArgumentType.class)
public class EntityArgumentTypeMixin {
	@Shadow @Final private boolean playersOnly;
	@Shadow @Final private boolean singleTarget;

	public EntitySelector fukkit$parse(StringReader stringReader, boolean overridePermissions) throws CommandSyntaxException {
		EntitySelectorReader entitySelectorReader = new EntitySelectorReader(stringReader);
		EntitySelector entitySelector = ((EntitySelectorReaderAccess) entitySelectorReader).parse(overridePermissions);
		if (entitySelector.getLimit() > 1 && this.singleTarget) {
			if (this.playersOnly) {
				stringReader.setCursor(0);
				throw TOO_MANY_PLAYERS_EXCEPTION.createWithContext(stringReader);
			} else {
				stringReader.setCursor(0);
				throw TOO_MANY_ENTITIES_EXCEPTION.createWithContext(stringReader);
			}
		} else if (entitySelector.includesNonPlayers() && this.playersOnly && !entitySelector.isSenderOnly()) {
			stringReader.setCursor(0);
			throw PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION.createWithContext(stringReader);
		} else {
			return entitySelector;
		}
	}
}
