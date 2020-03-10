package com.github.fukkitmc.fukkit.util.craftbukkit;

import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import java.util.ArrayList;

public class BlockStateArrayList extends ArrayList<CraftBlockState> {
	@Override
	public boolean add(CraftBlockState blockState) {
		for (BlockState blockState1 : this) {
			if (blockState1.getLocation().equals(blockState.getLocation())) { return false; }
		}
		return super.add(blockState);
	}
}
