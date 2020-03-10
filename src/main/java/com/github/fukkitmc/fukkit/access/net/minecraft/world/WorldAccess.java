package com.github.fukkitmc.fukkit.access.net.minecraft.world;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import com.github.fukkitmc.fukkit.util.craftbukkit.BlockStateArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.ChunkGenerator;
import java.util.List;

public interface WorldAccess extends CraftHandled<CraftWorld> {
	CraftServer getBukkitServer();

	WorldChunk getChunkIfLoaded(int x, int z);

	ChunkGenerator getGenerator();

	void setGenerator(ChunkGenerator generator);

	boolean keepSpawnInMemory();

	void setKeepSpawnInMemory(boolean keepSpawnInMemory);

	long getTicksPerAnimalSpawns();

	void setTicksPerAnimalSpawns(long ticks);

	long getTicksPerMonsterSpawns();

	void setTicksSpawnMonsterSpawns(long ticks);

	List<ItemEntity> getCaptureDrops();

	ChunkManager getChunkManager();

	void setChunkManager(ChunkManager manager);

	boolean hasPvp();

	void setPvp(boolean pvp);

	boolean capturesBlockStates();

	void setCaptureBlockStates(boolean captureBlockStates);

	boolean capturesTreeGen();

	void setCaptureTreeGen(boolean captureTreeGen);

	BlockStateArrayList getCapturedStates();

	boolean isPopulating();

	void setPopulating(boolean populating);

	boolean initialized();

	void setInitialized(boolean init);

	void notifyAndUpdatePhysics(BlockPos blockposition, WorldChunk chunk, BlockState oldBlock, BlockState newBlock,
	                            BlockState actualBlock, int i);

	BlockEntity getBlockEntity(BlockPos pos, boolean validate);
}
