package com.github.fukkitmc.fukkit.access.net.minecraft.world;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RayTraceContext;

public interface BlockViewAccess {
	BlockHitResult rayTraceBlock(RayTraceContext raytrace1, BlockPos blockposition);
}
