package com.github.fukkitmc.fukkit.mixin.net.minecraft.command.arguments;

import com.github.fukkitmc.fukkit.access.net.minecraft.command.arguments.EntityArgumentTypeAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.command.arguments.EntitySelectorReaderAccess;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.arguments.EntityArgumentType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.minecraft.command.arguments.EntityArgumentType.*;

@Mixin (EntityArgumentType.class)
public abstract class EntityArgumentTypeMixin implements EntityArgumentTypeAccess {

	@Shadow public abstract EntitySelector parse(StringReader stringReader) throws CommandSyntaxException;

	private static final ThreadLocal<Boolean> OVERRIDE_PERMISSIONS = ThreadLocal.withInitial(() -> false);
	@Redirect(method = "parse", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/EntitySelectorReader;read()Lnet/minecraft/command/EntitySelector;"))
	private EntitySelector parse(EntitySelectorReader reader, StringReader stringReader) throws CommandSyntaxException {
		EntitySelector selector = ((EntitySelectorReaderAccess)reader).parse(OVERRIDE_PERMISSIONS.get());
		OVERRIDE_PERMISSIONS.set(false);
		return selector;
	}

	@Override
	public EntitySelector parse(StringReader stringReader, boolean overridePermissions) throws CommandSyntaxException {
		OVERRIDE_PERMISSIONS.set(overridePermissions);
		return this.parse(stringReader);
	}
}
