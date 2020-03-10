package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (EndPortalBlock.class)
public class EndPortalBlockMixin {
	@Inject (method = "onEntityCollision", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/entity/Entity;changeDimension" +
	                                                          "(Lnet/minecraft/world/dimension/DimensionType;)" +
	                                                          "Lnet/minecraft/entity/Entity;"))
	private void fukkit_entityPortalEvent(BlockState state, World world, BlockPos pos, Entity entity,
	                                      CallbackInfo ci) {
		EntityPortalEnterEvent event = new EntityPortalEnterEvent(((EntityAccess<?>) entity)
		                                                          .getBukkit(),
		new org.bukkit.Location(((WorldAccess) world)
		                                                                                                .getBukkit(),
		pos
		                                                                                                              .getX(), pos
		                                                                                                                       .getY(), pos
		                                                                                                                                .getZ()));
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);

		if (entity instanceof ServerPlayerEntity) {
			((ServerPlayerEntityAccess) entity).changeDimension(
			((DimensionTypeAccess) world.dimension.getType()).getType() == DimensionType.THE_END ?
			DimensionType.OVERWORLD : DimensionType.THE_END, PlayerTeleportEvent.TeleportCause.END_PORTAL);
			ci.cancel();
			return;
		}
		entity.changeDimension(
		((DimensionTypeAccess) world.dimension.getType()).getType() == DimensionType.THE_END ? DimensionType.OVERWORLD :
		DimensionType.THE_END);
	}
}
