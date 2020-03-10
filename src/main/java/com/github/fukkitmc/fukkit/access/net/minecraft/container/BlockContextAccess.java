package com.github.fukkitmc.fukkit.access.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Location;

public interface BlockContextAccess {
	World getWorld();

	BlockPos getPosition();

	Location getLocation();
}
