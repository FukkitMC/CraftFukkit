package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.command.CommandOutputAccess;
import com.github.fukkitmc.fukkit.util.Constructors;
import com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.block.LecternInventory;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.container.LecternContainer;
import net.minecraft.container.PropertyDelegate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.CraftBlockCommandSender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (LecternBlockEntity.class)
public class LecternBlockEntityMixin implements CommandOutput, CommandOutputAccess {
	@Mutable @Shadow @Final private Inventory inventory;

	@Inject (method = "<init>()V",
	         at = @At ("TAIL"))
	private void fukkit_lecternBlockEntity(CallbackInfo ci) {
		this.inventory = new LecternInventory((LecternBlockEntity) (Object) this);
	}

	@Redirect (method = "getCommandSource",
	           at = @At (value = "FIELD",
	                     target = "Lnet/minecraft/server/command/CommandOutput;DUMMY:Lnet/minecraft/server/command/CommandOutput;"))
	private CommandOutput fukkit_this() {
		return this;
	}

	@Redirect (method = "createMenu",
	           at = @At (value = "NEW",
	                     target = "net.minecraft.container.LecternContainer"))
	private LecternContainer fukkit_addCtor(int syncId, Inventory inventory, PropertyDelegate propertyDelegate, int syncId2, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return Constructors.newLecternContainer(syncId, inventory, propertyDelegate, playerInventory);
	}

	@Override
	public void sendMessage(Text message) {}

	@Override
	public boolean sendCommandFeedback() {
		return false;
	}

	@Override
	public boolean shouldTrackOutput() {
		return false;
	}

	@Override
	public boolean shouldBroadcastConsoleToOps() {
		return false;
	}

	@Override
	public CommandSender getBukkitSender(ServerCommandSource wrapper) {
		return wrapper.getEntity() != null ? ((EntityAccess<?>) wrapper.getEntity()).getBukkitSender(wrapper) : new CraftBlockCommandSender(wrapper, (LecternBlockEntity) (Object) this);
	}
}
