package com.github.fukkitmc.fukkit.mixin.net.minecraft.command;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.command.ServerCommandSourceAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin {
	@Redirect(method = "checkSourcePermission", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"))
	private boolean fukkit_checkPerm(ServerCommandSource source, int level) {
		return !((ServerCommandSourceAccess)source).hasPermission(2, "minecraft.command.selector");
	}
}
