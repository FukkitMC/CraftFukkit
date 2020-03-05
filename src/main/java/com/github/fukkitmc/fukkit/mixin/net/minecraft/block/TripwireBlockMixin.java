package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.TripwireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.List;

@Mixin (TripwireBlock.class)
public class TripwireBlockMixin {
	@Inject (method = "updatePowered", at = @At (value = "JUMP", ordinal = 1, shift = At.Shift.AFTER), locals = LocalCapture.PRINT, cancellable = true)
	private void fukkit_interactEvent(World world, BlockPos pos, CallbackInfo ci, BlockState state, boolean powered, boolean shouldPower, List<? extends Entity> list) {
		if (shouldPower) { // powered != shouldPower is handled above
			org.bukkit.World bworld = ((WorldAccess) world).getBukkit();
			org.bukkit.plugin.PluginManager manager = ((WorldAccess) world).getBukkitServer().getPluginManager();
			org.bukkit.block.Block block = bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
			boolean allowed = false;

			// If all of the events are cancelled block the tripwire trigger, else allow
			for (Object object : list) {
				if (object != null) {
					org.bukkit.event.Cancellable cancellable;

					if (object instanceof PlayerEntity) {
						cancellable = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent((PlayerEntity) object, org.bukkit.event.block.Action.PHYSICAL, pos, null, null, null);
					} else if (object instanceof Entity) {
						cancellable = new EntityInteractEvent(((EntityAccess<?>) object).getBukkit(), block);
						manager.callEvent((EntityInteractEvent) cancellable);
					} else {
						continue;
					}

					if (!cancellable.isCancelled()) {
						allowed = true;
						break;
					}
				}
			}

			if (!allowed) {
				ci.cancel();
			}
		}
	}
}
