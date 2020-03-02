package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.dimension;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Implements(@Interface(iface = DimensionTypeAccess.class, prefix = "fukkit$"))
@Mixin(DimensionType.class)
public class DimensionTypeMixin {
	// TODO fix
	private DimensionType type;
	public DimensionType fukkit$getType() {
		return this.type == null ? (DimensionType) (Object) this : this.type;
	}

	public void fukkit$setType(DimensionType type) {
		this.type = type;
	}
}
