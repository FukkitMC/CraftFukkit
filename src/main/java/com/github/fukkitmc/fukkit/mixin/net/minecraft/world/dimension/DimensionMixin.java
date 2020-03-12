package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.dimension;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionAccess;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin (Dimension.class)
public class DimensionMixin implements DimensionAccess {
	@Shadow @Final private DimensionType type;

	/**
	 * craftbukkit told me to pls don't hurt me
	 *
	 * @author HalfOf2
	 */
	@Override
	@Overwrite
	public DimensionType getType() {
		return this.type;
	}
}
