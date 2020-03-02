package com.github.fukkitmc.fukkit.access.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.util.math.BlockPos;
import org.bukkit.World;

public interface BlockContextAccess {
	default World getWorld() {
		throw new UnsupportedOperationException("not suported");
	}

	default BlockPos getPosition() {
		throw new UnsupportedOperationException("not suported");
	}

	default org.bukkit.Location getLocation() {
		BlockPos pos = this.getPosition();
		return new org.bukkit.Location(((WorldAccess) this.getWorld()).getBukkit(), pos.getX(), pos.getY(), pos.getZ());
	}

}
