package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.dimension;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionAccess;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.TheNetherDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin (TheNetherDimension.class)
public abstract class TheNetherDimensionMixin extends Dimension {
	public TheNetherDimensionMixin(World world, DimensionType type, float f) {
		super(world, type, f);
	}

	/**
	 * Bukkit said this is needed, and so it must be done
	 *
	 * @author HalfOf2
	 */
	@Override
	@Overwrite
	public WorldBorder createWorldBorder() {
		return new WorldBorder();
	}

	/**
	 * handled by super
	 *
	 * @author HalfOf2
	 */
	@Override
	@Overwrite
	public DimensionType getType() {
		return ((DimensionAccess) this).getType();
	}
}
