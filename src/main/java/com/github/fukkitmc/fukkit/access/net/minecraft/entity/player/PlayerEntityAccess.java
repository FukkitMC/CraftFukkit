package com.github.fukkitmc.fukkit.access.net.minecraft.entity.player;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.LivingEntityAccess;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;

public interface PlayerEntityAccess<E extends CraftHumanEntity> extends LivingEntityAccess<E> {
	boolean isFauxSleeping();
	void setFauxSleeping(boolean fauxSleeping);
	String getSpawnWorld();
	void setSpawnWorld(String world);
	int getOldLevel();
	void setOldLevel(int level);
	Either<PlayerEntity.SleepFailureReason, Unit> sleep(BlockPos blockposition, boolean force);
}
