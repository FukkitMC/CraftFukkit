package com.github.fukkitmc.fukkit.access.net.minecraft.world.chunk;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Chunk;

public interface WorldChunkAccess extends CraftHandled<Chunk> {
	boolean getMustNotSave();

	void setMustNotSave(boolean save);

	boolean getNeedsDecoration();

	void setNeedsDecoration(boolean decoration);

	BlockState setType(BlockPos pos, BlockState state, boolean flag, boolean doPlace);
}
