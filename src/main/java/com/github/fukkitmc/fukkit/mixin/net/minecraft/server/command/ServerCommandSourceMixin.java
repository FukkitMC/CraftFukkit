package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.command;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.command.CommandOutputAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.command.ServerCommandSourceAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerCommandSource.class)
public abstract class ServerCommandSourceMixin implements ServerCommandSourceAccess {
	@Shadow public abstract ServerWorld getWorld();

	@Shadow @Final private int level;
	@Shadow @Final private CommandOutput output;
	private CommandNode<ServerCommandSource> currentCommand;

	@Redirect(method = "sendToOps", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getGameProfile()Lcom/mojang/authlib/GameProfile;"))
	private GameProfile fukkit_hasPermission(ServerPlayerEntity entity) {
		return ((ServerPlayerEntityAccess)entity).getBukkit().hasPermission("minecraft.admin.command_feedback") ? null : entity.getGameProfile();
	}

	@Redirect(method = "sendToOps", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;isOperator(Lcom/mojang/authlib/GameProfile;)Z"))
	private boolean fukkit_hasPerm(PlayerManager manager, GameProfile gameProfile) {
		return gameProfile == null;
	}


	@Inject(method = "hasPermissionLevel", at = @At("HEAD"), cancellable = true)
	private void fukkit_hasPermission(int level, CallbackInfoReturnable<Boolean> cir) {
		if(this.currentCommand != null)
			cir.setReturnValue(this.hasPermission(level, VanillaCommandWrapper.getPermission(this.currentCommand)));
	}
	@Override
	public void setCurrentCommand(CommandNode<ServerCommandSource> currentCommand) {
		this.currentCommand = currentCommand;
	}

	@Override
	public CommandNode<ServerCommandSource> getCurrentCommand() {
		return this.currentCommand;
	}

	@Override
	public boolean hasPermission(int level, String bukkitPermission) {
		return ((this.getWorld() == null || !((WorldAccess) this.getWorld()).getBukkitServer().ignoreVanillaPermissions) && this.level >= level) || this.getBukkitSender().hasPermission(bukkitPermission);
	}

	@Override
	public CommandSender getBukkitSender() {
		return ((CommandOutputAccess) this.output).getBukkitSender((ServerCommandSource) (Object) this);
	}
}
