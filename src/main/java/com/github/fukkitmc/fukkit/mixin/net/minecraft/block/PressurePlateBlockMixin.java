package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PressurePlateBlock.class)
public abstract class PressurePlateBlockMixin {
	@Shadow protected abstract int getRedstoneOutput(BlockState state);

	@Redirect (method = "getRedstoneOutput(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;canAvoidTraps()Z"))
	private boolean fukkit_interactEvent(Entity entity, World world, BlockPos pos) {
		if (this.getRedstoneOutput(world.getBlockState(pos)) == 0) {
			org.bukkit.World bworld = ((WorldAccess)world).getBukkit();
			org.bukkit.plugin.PluginManager manager = ((WorldAccess)world).getBukkitServer().getPluginManager();
			org.bukkit.event.Cancellable cancellable;

			if (entity instanceof PlayerEntity) {
				cancellable = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent((PlayerEntity) entity, org.bukkit.event.block.Action.PHYSICAL, pos, null, null, null);
			} else {
				cancellable = new EntityInteractEvent(((EntityAccess<?>)entity).getBukkit(), bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
				manager.callEvent((EntityInteractEvent) cancellable);
			}

			// We only want to block turning the plate on if all events are cancelled
			return cancellable.isCancelled(); // escape if
		}
		return false;
	}
}
