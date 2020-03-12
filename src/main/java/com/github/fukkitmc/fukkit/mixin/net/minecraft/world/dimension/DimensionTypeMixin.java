package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.dimension;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin (DimensionType.class)
public class DimensionTypeMixin implements DimensionTypeAccess {
	private DimensionType type;

	@Override
	public DimensionType getType() {
		return this.type == null ? (DimensionType) (Object) this : this.type;
	}

	@Override
	public void setType(DimensionType type) {
		this.type = type;
	}
}
