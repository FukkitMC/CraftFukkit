package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.ai.brain.task;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.task.VillagerBreedTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Optional;

@Mixin (VillagerBreedTask.class)
public class VillagerBreedTaskMixin {
	@Inject (method = "createChild",
	         at = @At (value = "INVOKE_ASSIGN",
	                   target = "Lnet/minecraft/entity/passive/VillagerEntity;createChild(Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/VillagerEntity;"),
	         locals = LocalCapture.CAPTURE_FAILHARD,
	         cancellable = true)
	private void fukkit_callBreed(VillagerEntity first, VillagerEntity second, CallbackInfoReturnable<Optional<VillagerEntity>> cir, VillagerEntity child) {
		if(CraftEventFactory.callEntityBreedEvent(child, first, second, null, null, 0).isCancelled())
			cir.setReturnValue(Optional.empty());
	}

	@Redirect(method = "createChild", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_spawnReason(World world, Entity entity) {
		return ((ServerWorldAccess)world).addEntity(entity, CreatureSpawnEvent.SpawnReason.BREEDING);
	}
}
