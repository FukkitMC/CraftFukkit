package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayNetworkHandlerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (EndGatewayBlockEntity.class)
public abstract class EndGatewayBlockEntityMixin extends BlockEntity {
	@Shadow public abstract boolean needsCooldownBeforeTeleporting();

	public EndGatewayBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject (method = "tryTeleportingEntity",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/entity/Entity;teleport(DDD)V"),
	         cancellable = true)
	private void fukkit_playerTeleportEvent(Entity entity, CallbackInfo ci) {
		if (entity instanceof PlayerEntity) {
			CraftPlayer player = (CraftPlayer) ((EntityAccess<?>) entity).getBukkit();
			Location location = new Location(((WorldAccess) world).getBukkit(), (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
			location.setPitch(player.getLocation().getPitch());
			location.setYaw(player.getLocation().getYaw());

			PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.END_GATEWAY);
			Bukkit.getPluginManager().callEvent(teleEvent);
			if (teleEvent.isCancelled()) {
				ci.cancel();
				return;
			}

			((ServerPlayNetworkHandlerAccess)((ServerPlayerEntity) entity).networkHandler).teleport(teleEvent.getTo());
			this.needsCooldownBeforeTeleporting(); // CraftBukkit - call at end of method
			ci.cancel();
		}
	}
}
