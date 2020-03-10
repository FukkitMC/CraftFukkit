package com.github.fukkitmc.fukkit.util.craftbukkit;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.PlayerManagerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.border.WorldBorderAccess;
import net.minecraft.network.packet.s2c.play.WorldBorderS2CPacket;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;

public class CraftWorldBorderListener implements WorldBorderListener {
	private final World world;
	private final WorldAccess access;
	// TODO implement

	public CraftWorldBorderListener(World world) {
		this.world = world;
		this.access = (WorldAccess) world;
	}

	@Override
	public void onSizeChange(WorldBorder worldBorder, double d) {
		((PlayerManagerAccess) this.access.getBukkitServer().getHandle())
		.sendAll(new WorldBorderS2CPacket(worldBorder, WorldBorderS2CPacket.Type.SET_SIZE),
		((WorldBorderAccess) worldBorder)
		                                                                                    .getServerWorld());
	}

	@Override
	public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
		((PlayerManagerAccess) this.access.getBukkitServer().getHandle())
		.sendAll(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.LERP_SIZE), ((WorldBorderAccess) border)
		                                                                                .getServerWorld());
	}

	@Override
	public void onCenterChanged(WorldBorder centerX, double centerZ, double d) {
		((PlayerManagerAccess) this.access.getBukkitServer().getHandle())
		.sendAll(new WorldBorderS2CPacket(centerX, WorldBorderS2CPacket.Type.SET_CENTER), ((WorldBorderAccess) centerX)
		                                                                                  .getServerWorld());
	}

	@Override
	public void onWarningTimeChanged(WorldBorder warningTime, int i) {
		((PlayerManagerAccess) this.access.getBukkitServer().getHandle())
		.sendAll(new WorldBorderS2CPacket(warningTime, WorldBorderS2CPacket.Type.SET_WARNING_TIME),
		((WorldBorderAccess) warningTime)
		                                                                                            .getServerWorld());
	}

	@Override
	public void onWarningBlocksChanged(WorldBorder warningBlocks, int i) {
		((PlayerManagerAccess) this.access.getBukkitServer().getHandle())
		.sendAll(new WorldBorderS2CPacket(warningBlocks, WorldBorderS2CPacket.Type.SET_WARNING_BLOCKS), ((WorldBorderAccess) warningBlocks)
		                                                                                                .getServerWorld());
	}

	@Override
	public void onDamagePerBlockChanged(WorldBorder damagePerBlock, double d) {
	}

	@Override
	public void onSafeZoneChanged(WorldBorder safeZoneRadius, double d) {

	}
}
