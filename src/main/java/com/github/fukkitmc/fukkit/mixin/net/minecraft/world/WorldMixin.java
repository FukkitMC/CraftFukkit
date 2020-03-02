package com.github.fukkitmc.fukkit.mixin.net.minecraft.world;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.border.WorldBorderAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.chunk.WorldChunkAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import com.github.fukkitmc.fukkit.util.craftbukkit.BlockStateArrayList;
import com.github.fukkitmc.fukkit.util.craftbukkit.CraftWorldBorderListener;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.level.LevelProperties;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.generator.ChunkGenerator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static net.minecraft.world.World.isHeightInvalid;

@Mixin (World.class)
@Implements (@Interface (iface = WorldAccess.class, prefix = "fukkit$"))
public abstract class WorldMixin implements IWorld {
	@Mutable
	@Shadow
	@Final
	protected ChunkManager chunkManager;

	@Override
	@Shadow
	public abstract WorldBorder getWorldBorder();

	@Shadow
	@Final
	public Dimension dimension;

	@Override
	@Shadow public abstract BlockState getBlockState(BlockPos pos);

	@Shadow @Final private Profiler profiler;

	@Override
	@Shadow public abstract ChunkManager getChunkManager();

	@Shadow public abstract void checkBlockRerender(BlockPos pos, BlockState old, BlockState updated);

	@Shadow @Final public boolean isClient;

	@Shadow public abstract void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags);

	@Override
	@Shadow public abstract void updateNeighbors(BlockPos pos, Block block);

	@Shadow public abstract void updateHorizontalAdjacent(BlockPos pos, Block block);

	@Shadow public abstract void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock);

	@Shadow @Final public List<BlockEntity> blockEntities;

	@Shadow public abstract boolean addBlockEntity(BlockEntity blockEntity);

	@Shadow @Final private Thread thread;
	@Shadow protected boolean iteratingTickingBlockEntities;

	@Shadow protected abstract BlockEntity getPendingBlockEntity(BlockPos blockPos);

	@Shadow public abstract WorldChunk getWorldChunk(BlockPos blockPos);

	@Shadow public abstract void setBlockEntity(BlockPos pos, BlockEntity blockEntity);

	@Shadow public abstract boolean isRaining();

	@Shadow protected float rainGradientPrev;
	@Shadow protected float rainGradient;
	@Shadow protected float thunderGradientPrev;
	@Shadow protected float thunderGradient;
	@Shadow @Final protected LevelProperties properties;

	@Override
	@Shadow public abstract World getWorld();

	@Shadow public abstract long getTimeOfDay();

	@Shadow public abstract GameRules getGameRules();

	@Shadow public abstract void setTimeOfDay(long time);

	@Shadow public abstract void tickEntity(Consumer<Entity> consumer, Entity entity);

	@Override
	@Shadow public abstract long getSeed();

	@Override
	@Shadow public abstract LevelProperties getLevelProperties();

	@Override
	@Shadow public abstract WorldChunk getChunk(int i, int j);

	@Override
	@Shadow public abstract Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);

	@Shadow public native Explosion createExplosion(Entity entity, DamageSource damageSource, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType);

	@Shadow public abstract void removeBlockEntity(BlockPos blockPos);

	private CraftWorld world;
	public boolean pvpMode;
	public boolean keepSpawnInMemory = true;
	public ChunkGenerator generator;
	public boolean captureBlockStates = false;
	public boolean captureTreeGeneration = false;
	public ArrayList<CraftBlockState> capturedBlockStates = new BlockStateArrayList();
	public List<ItemEntity> captureDrops;
	public long ticksPerAnimalSpawns;
	public long ticksPerMonsterSpawns;
	public boolean populating;
	private boolean initialized;

	@Inject (method = "<init>", at = @At ("TAIL"))
	public void after(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient, CallbackInfo ci) {
		((WorldBorderAccess) this.getWorldBorder()).setServerWorld((ServerWorld) (Object) this);
		this.getWorldBorder().addListener(new CraftWorldBorderListener((World) (Object) this));
	}

	@Inject (method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", at = @At ("HEAD"))
	private void fukkit_captureStates(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
		if (this.captureTreeGeneration) {
			CraftBlockState blockstate = null;
			Iterator<CraftBlockState> it = this.capturedBlockStates.iterator();
			while (it.hasNext()) {
				CraftBlockState previous = it.next();
				if (previous.getPosition().equals(pos)) {
					blockstate = previous;
					it.remove();
					break;
				}
			}
			if (blockstate == null) {
				blockstate = org.bukkit.craftbukkit.block.CraftBlockState.getBlockState((World) (Object) this, pos, flags);
			}
			blockstate.setData(state);
			this.capturedBlockStates.add(blockstate);
			cir.cancel();
		}
	}

	
	@Redirect (method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;"))
	private BlockState fukkit_captureStates0(WorldChunk chunk, BlockPos pos, BlockState state, boolean bl, BlockPos pos2, BlockState state2, int flags) {
		CraftBlockState craftState = null;
		if (this.captureBlockStates) {
			craftState = CraftBlockState.getBlockState((World) (Object) this, pos, flags);
			this.capturedBlockStates.add(craftState);
		}

		BlockState returned = ((WorldChunkAccess) chunk).setType(pos, state, bl, (flags & 1024) == 0);
		if(returned == null && this.captureBlockStates) {
			this.capturedBlockStates.remove(craftState);
		}
		return returned;
	}

	@Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_modPhysicUpdate(BlockPos pos, BlockState state, int i, CallbackInfoReturnable<Boolean> cir, WorldChunk chunk, Block block, BlockState otherState) {
		BlockState at = this.getBlockState(pos);
		if (at != otherState && (at.getOpacity((World) (Object) this, pos) != otherState.getOpacity((World) (Object) this, pos) || at.getLuminance() != otherState.getLuminance() || at.hasSidedTransparency() || otherState.hasSidedTransparency())) {
			this.profiler.push("queueCheckLight");
			this.getChunkManager().getLightingProvider().checkBlock(pos);
			this.profiler.pop();
		}

		if(!this.captureBlockStates) {
			this.fukkit$notifyAndUpdatePhysics(pos, chunk, otherState, state, at, i);
		}
	}

	public void fukkit$notifyAndUpdatePhysics(BlockPos blockposition, WorldChunk chunk, net.minecraft.block.BlockState oldBlock, net.minecraft.block.BlockState newBlock, net.minecraft.block.BlockState actualBlock, int i) {
		if (actualBlock == newBlock) {
			if (oldBlock != actualBlock) {
				this.checkBlockRerender(blockposition, oldBlock, actualBlock);
			}

			if ((i & 2) != 0 && (!this.isClient || (i & 4) == 0) && (this.isClient || chunk == null || (chunk.getLevelType() != null && chunk.getLevelType().isAfter(ChunkHolder.LevelType.TICKING)))) { // allow chunk to be null here as chunk.isReady() is false when we send our notification during block placement
				this.updateListeners(blockposition, oldBlock, newBlock, i);
			}

			if (!this.isClient && (i & 1) != 0) {
				this.updateNeighbors(blockposition, oldBlock.getBlock());
				if (newBlock.hasComparatorOutput()) {
					this.updateHorizontalAdjacent(blockposition, newBlock.getBlock());
				}
			}

			if ((i & 16) == 0) {
				int j = i & -2;

				// CraftBukkit start
				oldBlock.method_11637(this, blockposition, j); // Don't call an event for the old block to limit event spam
				CraftWorld world = ((ServerWorldAccess) this).getBukkit();
				if (world != null) {
					BlockPhysicsEvent event = new BlockPhysicsEvent(world.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), CraftBlockData.fromData(newBlock));
					((ServerWorldAccess) this).getBukkitServer().getPluginManager().callEvent(event);

					if (event.isCancelled()) {
						return;
					}
				}
				// CraftBukkit end
				newBlock.updateNeighborStates(this, blockposition, j);
				newBlock.method_11637(this, blockposition, j);
			}

			this.onBlockChanged(blockposition, oldBlock, actualBlock);
		}
	}

	@Inject(method = "updateNeighbors", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateNeighborsAlways(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
	private void fukkit_populating(BlockPos pos, Block block, CallbackInfo ci) {
		if(this.populating)
			ci.cancel();
	}

	@Inject(method = "updateNeighbor", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;neighborUpdate(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_blockPhysics(BlockPos sourcePos, Block sourceBlock, BlockPos neighborPos, CallbackInfo ci, BlockState state) {
		CraftWorld world = ((ServerWorldAccess)this).getBukkit();
		if(world != null) {
			BlockPhysicsEvent event = new BlockPhysicsEvent(world.getBlockAt(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ()), CraftBlockData.fromData(state), world.getBlockAt(neighborPos.getX(), neighborPos.getY(), neighborPos.getZ()));
			((ServerWorldAccess) this).getBukkitServer().getPluginManager().callEvent(event);
			if(event.isCancelled())
				ci.cancel();
		}
	}

	@Inject(method = "getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"))
	private void fukkit_capture(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		if(this.captureTreeGeneration) {
			for (CraftBlockState state : this.capturedBlockStates) {
				if(state.getPosition().equals(pos)) {
					cir.setReturnValue(state.getHandle());
				}
			}
		}
	}

	@Redirect(method = "isDay", at =  @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getType()Lnet/minecraft/world/dimension/DimensionType;"))
	private DimensionType fukkit_getType(Dimension dimension) {
		return ((DimensionTypeAccess)dimension.getType()).getType();
	}
	@Redirect(method = "isNight", at =  @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getType()Lnet/minecraft/world/dimension/DimensionType;"))
	private DimensionType fukkit_getType0(Dimension dimension) {
		return ((DimensionTypeAccess)dimension.getType()).getType();
	}

	@Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
	private boolean fukkit_movedDown(List list, Object o) {
		return true; // force if statement to exit
	}

	// not exact, is injected before, not after :ohno:
	@Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateListeners(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_fromAbove(CallbackInfo ci, Profiler profiler, int i, BlockEntity entity, WorldChunk worldChunk, BlockState blockState) {
		if(!this.blockEntities.contains(entity)) {
			this.addBlockEntity(entity);
		}
		// don't screw this up - md5 probably
	}

	/**
	 * @author HalfOf2
	 * @reason functionality replaced
	 * TODO inline this with injects
	 */
	@Override
	@Overwrite
	public BlockEntity getBlockEntity(BlockPos pos) {
		return this.fukkit$getBlockEntity(pos, true);
	}

	public Map<BlockPos, BlockEntity> capturedBlockEntities = Maps.newHashMap();

	public BlockEntity fukkit$getBlockEntity(BlockPos blockposition, boolean validate) {
		// CraftBukkit end
		if (isHeightInvalid(blockposition)) {
			return null;
		} else if (!this.isClient && Thread.currentThread() != this.thread) {
			return null;
		} else {
			// CraftBukkit start
			if (this.capturedBlockEntities.containsKey(blockposition)) {
				return this.capturedBlockEntities.get(blockposition);
			}
			// CraftBukkit end

			BlockEntity blockEntity = null;

			if (this.iteratingTickingBlockEntities) {
				blockEntity = this.getPendingBlockEntity(blockposition);
			}

			if (blockEntity == null) {
				blockEntity = this.getWorldChunk(blockposition).getBlockEntity(blockposition, WorldChunk.CreationType.IMMEDIATE);
			}

			if (blockEntity == null) {
				blockEntity = this.getPendingBlockEntity(blockposition);
			}

			return blockEntity;
		}
	}

	@Inject(method = "setBlockEntity", at = @At(value = "JUMP", ordinal = 2))
	private void fukkit_capture(BlockPos pos, BlockEntity blockEntity, CallbackInfo ci) {
		if(this.captureBlockStates) {
			blockEntity.setLocation((World) (Object) this, pos);
			this.capturedBlockEntities.put(pos, blockEntity);
			ci.cancel();
		}
	}

	@Redirect(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;"))
	private BlockEntity fukkit_redirect(World world, BlockPos pos) {
		return this.fukkit$getBlockEntity(pos, false);
	}

	public Object fukkit$getBukkit() {
		return this.world;
	}

	public void fukkit$setBukkit(Object world) {
		this.world = (CraftWorld) world;
	}

	public CraftServer fukkit$getBukkitServer() {
		return (CraftServer) Bukkit.getServer();
	}

	public WorldChunk fukkit$getChunkIfLoaded(int x, int z) {
		return this.chunkManager.getWorldChunk(x, z, false);
	}

	public ChunkGenerator fukkit$getGenerator() {
		return this.generator;
	}

	public void fukkit$setGenerator(ChunkGenerator generator) {
		this.generator = generator;
	}

	public boolean fukkit$keepSpawnInMemory() {
		return this.keepSpawnInMemory;
	}

	public void fukkit$setKeepSpawnInMemory(boolean keepSpawnInMemory) {
		this.keepSpawnInMemory = keepSpawnInMemory;
	}

	public long fukkit$getTicksPerAnimalSpawns() {
		return this.ticksPerAnimalSpawns;
	}

	public long fukkit$getTicksPerMonsterSpawns() {
		return this.ticksPerMonsterSpawns;
	}

	public void fukkit$setTicksPerAnimalSpawns(long ticks) {
		this.ticksPerAnimalSpawns = ticks;
	}

	public void fukkit$setTicksSpawnMonsterSpawns(long ticks) {
		this.ticksPerMonsterSpawns = ticks;
	}

	public List<ItemEntity> fukkit$getCaptureDrops() {return this.captureDrops;}

	public ChunkManager fukkit$getChunkManager() {
		return this.chunkManager;
	}

	public void fukkit$setChunkManager(ChunkManager manager) {
		this.chunkManager = manager;
	}

	public boolean fukkit$hasPvp() {
		return this.pvpMode;
	}

	public void fukkit$setPvp(boolean pvp) {
		this.pvpMode = pvp;
	}

	public boolean fukkit$capturesBlockStates() {
		return this.captureBlockStates;
	}

	public void fukkit$setCaptureBlockStates(boolean captureBlockStates) {
		this.captureBlockStates = captureBlockStates;
	}

	public boolean fukkit$capturesTreeGen() {
		return this.captureTreeGeneration;
	}

	public void fukkit$setCaptureTreeGen(boolean captureTreeGen) {
		this.captureTreeGeneration = captureTreeGen;
	}

	public List<CraftBlockState> fukkit$getCapturedStates() {
		return this.capturedBlockStates;
	}

	public boolean fukkit$isPopulating() {
		return this.populating;
	}

	public void fukkit$setPopulating(boolean populating) {
		this.populating = populating;
	}

	public boolean fukkit$initialized() {
		return this.initialized;
	}

	public void fukkit$setInitialized(boolean init) {
		this.initialized = init;
	}

}
