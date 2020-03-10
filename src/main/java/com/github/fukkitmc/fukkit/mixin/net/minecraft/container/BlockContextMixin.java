package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockContext.class)
public interface BlockContextMixin extends BlockContextAccess { // y a y
	@Override
	default World getWorld() {
		throw new UnsupportedOperationException();
	}

	@Override
	default BlockPos getPosition() {
		throw new UnsupportedOperationException();
	}

	@Override
	default Location getLocation() {
		BlockPos pos = this.getPosition();
		return new Location(((WorldAccess) this.getWorld()).getBukkit(), pos.getX(), pos.getY(), pos.getZ());
	}
}
