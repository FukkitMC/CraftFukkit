package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.world;

import net.minecraft.server.world.SecondaryServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import java.util.function.BiFunction;

@Mixin (SecondaryServerWorld.class)
public abstract class SecondaryServerWorldMixin extends World {

	protected SecondaryServerWorldMixin(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World
	                                                                                                            ,
	                                                                                                            Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient) {
		super(levelProperties, dimensionType, chunkManagerProvider, profiler, isClient);
	}

	/**
	 * @author HalfOf2
	 * bukkit pls
	 */
	@Override
	@Overwrite
	public void tickTime() {
		this.setTime(this.properties.getTime() + 1L);
		if (this.properties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
			this.setTimeOfDay(this.properties.getTimeOfDay() + 1L);
		}
	}

}
