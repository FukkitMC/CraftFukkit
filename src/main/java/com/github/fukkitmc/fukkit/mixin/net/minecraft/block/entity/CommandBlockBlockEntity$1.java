package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;


import com.github.fukkitmc.fukkit.access.net.minecraft.server.command.CommandOutputAccess;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.CommandBlockExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.CraftBlockCommandSender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin (targets = "net/minecraft/block/entity/CommandBlockBlockEntity$1")
public abstract class CommandBlockBlockEntity$1 extends CommandBlockExecutor implements CommandOutputAccess {
	@SuppressWarnings ("ShadowTarget") @Shadow @Final /*synthetic*/ CommandBlockBlockEntity field_11921;

	@Override
	public CommandSender getBukkitSender(ServerCommandSource wrapper) {
		return new CraftBlockCommandSender(wrapper, this.field_11921);
	}
}
