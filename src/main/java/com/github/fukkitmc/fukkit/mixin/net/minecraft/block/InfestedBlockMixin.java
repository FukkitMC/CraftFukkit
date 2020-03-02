package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.InfestedBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InfestedBlock.class)
public class InfestedBlockMixin {
	@Redirect (method = "onStacksDropped", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_addSpawnReason(World world, Entity entity) {
		if(!(world instanceof ServerWorld)) {
			throw new IllegalStateException("World is not serverworld!");
		}
		return ((ServerWorldAccess)world).addEntity(entity, CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK);
	}
}
