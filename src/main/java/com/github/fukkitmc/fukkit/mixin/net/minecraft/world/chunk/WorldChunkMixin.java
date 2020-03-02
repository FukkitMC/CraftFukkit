package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.chunk;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.chunk.WorldChunkAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.spongepowered.asm.mixin.*;
import java.util.Map;

@Implements (@Interface (iface = WorldChunkAccess.class, prefix = "fukkit$"))
@Mixin (WorldChunk.class)
public abstract class WorldChunkMixin {
	@Shadow
	@Final
	private ChunkSection[] sections;
	@Shadow
	@Final
	private Map<Heightmap.Type, Heightmap> heightmaps;
	@Shadow
	@Final
	private World world;
	@Shadow
	private volatile boolean shouldSave;

	@Shadow
	public abstract BlockEntity getBlockEntity(BlockPos pos, WorldChunk.CreationType creationType);

	private Chunk chunk = new CraftChunk((WorldChunk) (Object) this);
	private boolean mustNotSave;
	private boolean needsDecoration = true;

	public Object fukkit$getBukkit() {
		return this.chunk;
	}

	public void fukkit$setBukkit(Object object) {
		this.chunk = (Chunk) object;
	}

	public boolean fukkit$getMustNotSave() {
		return this.mustNotSave;
	}

	public void fukkit$setMustNotSave(boolean object) {
		this.mustNotSave = object;
	}

	public boolean fukkit$getNeedsDecoration() {
		return this.needsDecoration;
	}

	public void fukkit$setNeedsDecoration(boolean object) {
		this.needsDecoration = object;
	}

	public BlockState fukkit$setType(BlockPos pos, BlockState state, boolean flag, boolean doPlace) {
		int i = pos.getX() & 15;
		int j = pos.getY();
		int k = pos.getZ() & 15;
		ChunkSection chunksection = this.sections[j >> 4];

		if (chunksection == WorldChunk.EMPTY_SECTION) {
			if (state.isAir()) {
				return null;
			}

			chunksection = new ChunkSection(j >> 4 << 4);
			this.sections[j >> 4] = chunksection;
		}

		boolean chunkIsEmpty = chunksection.isEmpty();
		BlockState state1 = chunksection.setBlockState(i, j & 15, k, state);

		if (state1 == state) {
			return null;
		} else {
			Block block = state.getBlock();
			Block block1 = state1.getBlock();

			this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING).trackUpdate(i, j, k, state); // assumed from a -> trackUpdate
			this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).trackUpdate(i, j, k, state); // ^
			this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR).trackUpdate(i, j, k, state); // ^
			this.heightmaps.get(Heightmap.Type.WORLD_SURFACE).trackUpdate(i, j, k, state); // ^
			boolean newChunkIsEmpty = chunksection.isEmpty();

			if (chunkIsEmpty != newChunkIsEmpty) {
				this.world.getChunkManager().getLightingProvider().updateSectionStatus(pos, newChunkIsEmpty);
			}

			if (!this.world.isClient) {
				state1.onBlockRemoved(this.world, pos, state, flag);
			} else if (block1 != block && block1 instanceof BlockEntityProvider) {
				this.world.removeBlockEntity(pos);
			}

			if (chunksection.getBlockState(i, j & 15, k).getBlock() != block) {
				return null;
			} else {
				BlockEntity tileentity;

				if (block1 instanceof BlockEntityProvider) {
					tileentity = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
					if (tileentity != null) {
						tileentity.resetBlock();
					}
				}

				// CraftBukkit - Don't place while processing the BlockPlaceEvent, unless it's a BlockContainer. Prevents blocks such as TNT from activating when cancelled.
				//if (!this.world.isClient && doPlace && (!this.world.captureBlockStates || block instanceof BlockWithEntity)) {
				state.onBlockAdded(this.world, pos, state1, flag);
				//}
				// TODO fix

				if (block instanceof BlockEntityProvider) {
					tileentity = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
					if (tileentity == null) {
						tileentity = ((BlockEntityProvider) block).createBlockEntity(this.world);
						this.world.setBlockEntity(pos, tileentity);
					} else {
						tileentity.resetBlock();
					}
				}

				this.shouldSave = true;
				return state1;
			}
		}
	}
}
