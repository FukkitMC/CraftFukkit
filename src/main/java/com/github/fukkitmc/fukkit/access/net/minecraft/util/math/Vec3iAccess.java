package com.github.fukkitmc.fukkit.access.net.minecraft.util.math;

import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// TODO add to mixins.json
@Mixin(Vec3i.class)
public interface Vec3iAccess {
	@Accessor("x")
	void setX(int x);
	@Accessor("y")
	void setY(int y);
	@Accessor("z")
	void setZ(int z);
}
