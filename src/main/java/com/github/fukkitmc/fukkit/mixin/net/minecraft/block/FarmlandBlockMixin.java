package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (FarmlandBlock.class)
public class FarmlandBlockMixin extends Block {
	public FarmlandBlockMixin(Settings settings) {
		super(settings);
	}

	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_moistEvent(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleMoistureChangeEvent(world, pos, state, flags);
	}

	@Inject (method = "onLandedUpon", at = @At ("HEAD"))
	private void fukkit_gameNotEffect(World world, BlockPos pos, Entity entity, float distance, CallbackInfo ci) {
		super.onLandedUpon(world, pos, entity, distance);
	}

	@Inject (method = "onLandedUpon", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"), cancellable = true)
	private void fukkit_interactEvent(World world, BlockPos pos, Entity entity, float distance, CallbackInfo ci) {
		org.bukkit.event.Cancellable cancellable;
		if (entity instanceof PlayerEntity) {
			cancellable = CraftEventFactory.callPlayerInteractEvent((PlayerEntity) entity, org.bukkit.event.block.Action.PHYSICAL, pos, null, null, null);
		} else {
			cancellable = new EntityInteractEvent(((EntityAccess<?>)entity).getBukkit(), ((WorldAccess)world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
			((WorldAccess)world).getBukkitServer().getPluginManager().callEvent((EntityInteractEvent) cancellable);
		}

		if (cancellable.isCancelled()) {
			ci.cancel();
			return;
		}

		if (CraftEventFactory.callEntityChangeBlockEvent(entity, pos, Blocks.DIRT.getDefaultState()).isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(method = "setToDirt", at = @At("HEAD"), cancellable = true)
	private static void fukkit_callFadeEvent(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		if(CraftEventFactory.callBlockFadeEvent(world, pos, Blocks.DIRT.getDefaultState()).isCancelled())
			ci.cancel();
	}
}
