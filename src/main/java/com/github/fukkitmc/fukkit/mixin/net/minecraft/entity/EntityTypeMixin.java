package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityTypeAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Implements (@Interface (iface = EntityTypeAccess.class, prefix = "fukkit$"))
@Mixin (EntityType.class)
public abstract class EntityTypeMixin<T extends Entity> {
	/**
	 * @author HalfOf2
	 */
	@SuppressWarnings ("OverwriteModifiers")
	@Overwrite
	public T spawn(World world, CompoundTag itemTag, Text name, PlayerEntity player, BlockPos pos, SpawnType spawnType
	, boolean alignPosition, boolean invertY) {
		return this
		       .spawnCreature(world, itemTag, name, player, pos, spawnType, alignPosition, invertY,
		       CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
	}

	public T spawnCreature(World world, CompoundTag tag, Text text, PlayerEntity player, BlockPos pos, SpawnType type,
	                       boolean alignPosition, boolean invertY,
	                       org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
		T entity = this.create(world, tag, text, player, pos, type, alignPosition, invertY);
		return ((ServerWorldAccess) world).addEntity(entity, spawnReason) ? entity :
		       null; // Don't return an entity when CreatureSpawnEvent is canceled
		// CraftBukkit end
	}

	@Shadow
	public abstract T create(World world, CompoundTag itemTag, Text name, PlayerEntity player, BlockPos pos,
	                         SpawnType spawnType, boolean alignPosition, boolean invertY);

	@Redirect (method = "getTrackTickInterval", at = @At (value = "FIELD",
	                                                      target = "Lnet/minecraft/entity/EntityType;" +
	                                                               "AREA_EFFECT_CLOUD:Lnet/minecraft/entity" +
	                                                               "/EntityType;"))
	private EntityType<?> fukkit_areaEffect() {
		return EntityType.PAINTING; // I know right, I'm a genius
	}
}
