package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.dimension;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin (OverworldDimension.class)
public abstract class OverworldDimensionMixin extends Dimension {
	public OverworldDimensionMixin(World world, DimensionType type, float f) {
		super(world, type, f);
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
