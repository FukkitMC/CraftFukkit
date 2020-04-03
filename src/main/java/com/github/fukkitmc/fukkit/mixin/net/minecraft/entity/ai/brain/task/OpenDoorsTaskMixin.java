package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.ai.brain.task;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.LivingEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.List;
import java.util.Set;

@Mixin (OpenDoorsTask.class)
public class OpenDoorsTaskMixin {
	private static LivingEntity captured;
	@Inject(method = "findAndCloseOpenedDoors", at = @At("HEAD"))
	private void capture_entity(ServerWorld world, List<BlockPos> path, Set<BlockPos> doors, int lastNodeIndex, LivingEntity entity, Brain<?> brain, CallbackInfo ci) {
		captured = entity;
	}

	@Inject (method = "method_21696(Ljava/util/List;Lnet/minecraft/server/world/ServerWorld;ILnet/minecraft/entity/ai/brain/Brain;Lnet/minecraft/util/math/BlockPos;)V",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/block/DoorBlock;setOpen(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)V"),
	         cancellable = true)
	private static void fukkit_entityInteractEvent(List<?> list, ServerWorld world, int i, Brain<?> brain, BlockPos pos, CallbackInfo ci) {
		EntityInteractEvent event = new EntityInteractEvent(((LivingEntityAccess<?>)captured).getBukkit(), CraftBlock.at(world, pos));
		((WorldAccess)captured.world).getBukkitServer().getPluginManager().callEvent(event);
		if(event.isCancelled())
			ci.cancel();
		captured = null;
	}
}
