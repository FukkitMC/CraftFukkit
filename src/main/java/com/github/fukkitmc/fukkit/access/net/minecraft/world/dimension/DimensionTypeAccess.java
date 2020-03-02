package com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension;

import net.minecraft.world.dimension.DimensionType;

public interface DimensionTypeAccess {
	DimensionType getType();
	void setType(DimensionType type);
}
