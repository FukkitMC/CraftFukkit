package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Random;

@Mixin(TurtleEggBlock.class)
public class TurtleEggBlockMixin {
	@Shadow @Final public static IntProperty HATCH;

	@Inject(method = "tryBreakEgg", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/TurtleEggBlock;breakEgg(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"), cancellable = true)
	private void fukkit_interactEvent(World world, BlockPos pos, Entity entity, int inverseChance, CallbackInfo ci) {
		Cancellable cancellable;
		if(entity instanceof PlayerEntity) {
			cancellable = CraftEventFactory.callPlayerInteractEvent((PlayerEntity) entity, Action.PHYSICAL, pos, null, null, null);
		} else {
			cancellable = new EntityInteractEvent(((EntityAccess<?>)entity).getBukkit(), CraftBlock.at(world, pos));
			((WorldAccess)world).getBukkitServer().getPluginManager().callEvent((Event) cancellable);
		}

		if(cancellable.isCancelled())
			ci.cancel();
	}

	@Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V", ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_growEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci, int i) {
		if(!CraftEventFactory.handleBlockGrowEvent(world, pos, state.with(TurtleEggBlock.HATCH, i+1), 2))
			ci.cancel();
	}

	@Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_handledAbove(ServerWorld world, BlockPos pos, BlockState state, int flags) {return false;}

	@Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V", ordinal = 1), cancellable = true)
	private void fukkit_growEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if(!CraftEventFactory.callBlockFadeEvent(world, pos, Blocks.AIR.getDefaultState()).isCancelled())
			ci.cancel();
	}

	@Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_addReason(ServerWorld world, Entity entity) {
		return ((ServerWorldAccess)world).addEntity(entity, CreatureSpawnEvent.SpawnReason.EGG);
	}
}
