package com.github.fukkitmc.fukkit.access.net.minecraft.world;

import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface PortalForcerAccess {
	BlockPattern.TeleportTarget findAndTeleport(Entity entity, BlockPos pos, float yawOffset, int searchRadius,
	                                            boolean searchOnly);

	BlockPattern.TeleportTarget findPortal(BlockPos pos, Vec3d vec3d, Direction direction, double portalOffsetX,
	                                       double portalOffsetZ, boolean isHuman, int searchRadius);

	boolean createPortal(Entity entity, BlockPos createPos, int createRadius);
}
