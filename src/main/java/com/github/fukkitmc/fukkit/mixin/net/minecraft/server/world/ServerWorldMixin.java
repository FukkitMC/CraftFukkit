package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.world;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.entity.BlockEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.mob.DrownedEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.item.map.MapStateAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.explosion.ExplosionAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.level.LevelPropertiesAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.world.WorldMixin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.map.MapState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnGlobalS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;

@Mixin (ServerWorld.class)
@Implements (@Interface (iface = ServerWorldAccess.class, prefix = "fukkit$"))
public abstract class ServerWorldMixin extends WorldMixin {
	@Mutable
	@Shadow
	@Final
	private WanderingTraderManager wanderingTraderManager;

	// private int tickPosition; note: private field isn't accessed anywhere in the class, so I have no idea why it's there...

	@Shadow
	public abstract MinecraftServer getServer();

	@Shadow
	@Final
	private List<ServerPlayerEntity> players;

	@Shadow
	private boolean allPlayersSleeping;

	@Shadow
	protected abstract void method_23660();


	@Shadow
	@Final
	private List<Entity> globalEntities;

	@Shadow
	@Final
	private MinecraftServer server;

	@Shadow
	public abstract WorldSaveHandler getSaveHandler();

	@Shadow
	protected abstract boolean checkUuid(Entity entity);

	@Shadow
	protected abstract void loadEntityUnchecked(Entity entity);

	@Shadow
	@Final
	private Set<EntityNavigation> entityNavigations;

	@Shadow
	public abstract Entity getEntityById(int id);

	@Shadow
	private boolean insideTick;

	@Shadow
	protected abstract void resetWeather();

	@Shadow
	protected abstract boolean sendToPlayerIfNearby(ServerPlayerEntity player, boolean force, double x, double y, double z, Packet<?> packet);

	// mixin doesn't recognize the normal World#thread :irritatered:
	private Thread mixin_pls;

	@Inject (method = "<init>", at = @At ("TAIL"))
	private void fukkit_pvp(MinecraftServer server, Executor workerExecutor, WorldSaveHandler worldSaveHandler, LevelProperties properties, DimensionType dimensionType, Profiler profiler, WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
		((WorldAccess) this).setPvp(server.isPvpEnabled());
		((LevelPropertiesAccess) properties).setServerWorld((ServerWorld) (Object) this);
		this.wanderingTraderManager = ((DimensionTypeAccess) this.dimension.getType()).getType() == DimensionType.OVERWORLD ? new WanderingTraderManager((ServerWorld) (Object) this) : null;
		((WorldAccess) this).getBukkitServer().addWorld(((WorldAccess) this).getBukkit());
		this.mixin_pls = Thread.currentThread();
	}

	public BlockEntity fukkit$getBlockEntity(BlockPos pos, boolean validate) {
		BlockEntity result = super.fukkit$getBlockEntity(pos, validate);
		if (!validate || Thread.currentThread() != this.mixin_pls) {
			// SPIGOT-5378: avoid deadlock, this can be called in loading logic (i.e lighting) but getType() will block on chunk load
			return result;
		}
		Block type = this.getBlockState(pos).getBlock();

		if (result != null && type != Blocks.AIR) {
			if (!result.getType().supports(type)) {
				result = this.fukkit$fixBlockEntity(pos, type, result);
			}
		}

		return result;
	}

	public BlockEntity fukkit$fixBlockEntity(BlockPos pos, Block type, BlockEntity found) {
		this.fukkit$getBukkitServer().getLogger().log(Level.SEVERE, "Block at {0}, {1}, {2} is {3} but has {4}" + ". " + "Bukkit will attempt to fix this, but there may be additional damage that we cannot recover.", new Object[]{pos.getX(), pos.getY(), pos.getZ(), type, found});

		if (type instanceof BlockEntityProvider) {
			BlockEntity replacement = ((BlockEntityProvider) type).createBlockEntity((World) (Object) this);
			((BlockEntityAccess) replacement).setWorld((World) (Object) this);
			this.setBlockEntity(pos, replacement);
			return replacement;
		} else {
			return found;
		}
	}

	// two birds 1 stone
	@Redirect (method = "tick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToDimension(Lnet/minecraft/network/Packet;Lnet/minecraft/world/dimension/DimensionType;)V"))
	private void fukkit_delete(PlayerManager manager, Packet<?> packet, DimensionType dimension) {}

	// 99.999999999% safe
	private boolean oldRaining;

	@Inject (method = "tick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isRaining()Z", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_cursed(BooleanSupplier shouldKeepTicking, CallbackInfo ci, Profiler profiler, boolean bl) {
		this.oldRaining = bl;
		//...
		// end of if

		for (int i = 0; i < this.players.size(); i++) {
			ServerPlayerEntity entity = this.players.get(i);
			if (entity.world == (Object) this) {
				((ServerPlayerEntityAccess) entity).tickWeather();
			}
		}

		if (bl != this.isRaining()) {
			for (ServerPlayerEntity player : this.players) {
				if (player.world == (Object) this) {
					((ServerPlayerEntityAccess) player).setPlayerWeather((!bl ? WeatherType.DOWNFALL : WeatherType.CLEAR), false);
				}
			}
		}

		for (ServerPlayerEntity player : this.players) {
			if (player.world == (Object) this) {
				((ServerPlayerEntityAccess) player).updateWeather(this.rainGradientPrev, this.rainGradient, this.thunderGradientPrev, this.thunderGradient);
			}
		}
	}

	@Redirect (method = "tick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isRaining()Z", ordinal = 1))
	private boolean fukkit_forceFalseIf(ServerWorld world) {
		return this.oldRaining; // force false
	}

	@Inject (method = "method_18775(Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSleepingLongEnough()Z"))
	private static /*synthetic*/ void fukkit_sleeping(ServerPlayerEntity entity, CallbackInfoReturnable<Boolean> callback) {
		callback.setReturnValue(!entity.isSleepingLongEnough() && !((PlayerEntityAccess) entity).isFauxSleeping());
	}

	@Redirect (method = "tick", at = @At (value = "FIELD", target = "Lnet/minecraft/server/world/ServerWorld;allPlayersSleeping:Z", ordinal = 0, opcode = Opcodes.PUTFIELD))
	private void fukkit_nullifyAccess(ServerWorld world, boolean haha) {}

	@Redirect (method = "tick", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
	private boolean fukkit_timeSkipEvent(GameRules rules, GameRules.RuleKey<GameRules.BooleanRule> rule) {
		long l = this.properties.getTimeOfDay() + 24000L;
		TimeSkipEvent event = new TimeSkipEvent((org.bukkit.World) this.fukkit$getBukkit(), TimeSkipEvent.SkipReason.NIGHT_SKIP, (l - l % 24000L) - this.getTimeOfDay());
		if (rules.getBoolean(rule)) {
			this.fukkit$getBukkitServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				this.setTimeOfDay(this.getTimeOfDay() + event.getSkipAmount());
			}
		}

		if (!event.isCancelled()) {
			this.allPlayersSleeping = false;
			this.method_23660();
		}
		return false;
	}

	@Redirect (method = "tick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_23660()V"))
	private void fukkit_nullCall(ServerWorld world) {}

	@Redirect (method = "tick", at = @At (value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
	private boolean fukkit_preventEntityCleanup(List list) { // :notvanilla:
		return false;
	}

	// hack: replace with redirect to tickEntity and entity.removed
	@Redirect (method = "tick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickEntity(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V", ordinal = 0))
	private void fukkit_npe(ServerWorld world, Consumer<Entity> consumer, Entity entity) {
		if (entity != null) {
			this.tickEntity(consumer, entity);
		}
	}

	@Redirect (method = "tick", at = @At (value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/Entity;removed:Z"))
	private boolean fukkit_npe(Entity entity) {
		if (entity != null) {
			return entity.removed;
		}
		return false;
	}

	@Redirect (method = "tick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;shouldSpawnNpcs()Z"))
	private boolean fukkit_disable(MinecraftServer server) {
		return true;
	}

	@Redirect (method = "tick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;shouldSpawnAnimals()Z"))
	private boolean fukkit_should(MinecraftServer server) {
		return true;
	}

	@Redirect (method = "tickChunk", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_spawnReason(ServerWorld world, Entity entity) {
		return this.fukkit$addEntity(entity, CreatureSpawnEvent.SpawnReason.LIGHTNING);
	}

	@Redirect (method = "tickChunk", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;addLightning(Lnet/minecraft/entity/LightningEntity;)V"))
	private void fukkit_lightning(ServerWorld world, LightningEntity lightningEntity) {
		this.fukkit$strikeLightning(lightningEntity, LightningStrikeEvent.Cause.WEATHER);
	}

	@Redirect (method = "updatePlayersSleeping", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
	private boolean fukkit_fauxStuff(ServerPlayerEntity entity) {
		return entity.isSpectator() || (((ServerPlayerEntityAccess) entity).isFauxSleeping() && !entity.isSleeping());
	}

	@Redirect (method = "resetWeather", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;setRainTime(I)V"))
	private void fukkit_voidCall(LevelProperties properties, int rainTime) {}

	// not vanilla
	@Inject (method = "resetWeather", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;setRaining(Z)V", shift = At.Shift.AFTER))
	private void fukkit_setRain(CallbackInfo ci) {
		if (!this.properties.isRaining()) {
			this.properties.setRainTime(0);
		}
	}

	@Redirect (method = "resetWeather", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;setThunderTime(I)V"))
	private void fukkit_voidCall0(LevelProperties properties, int thunderTime) {}

	@Inject (method = "resetWeather", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;setThundering(Z)V", shift = At.Shift.AFTER))
	private void fukkit_setThunder(CallbackInfo ci) {
		if (!this.properties.isThundering()) {
			this.properties.setThunderTime(0);
		}
	}

	@Inject (method = "tickEntity", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V", shift = At.Shift.AFTER))
	private void fukkit_postTick(Entity entity, CallbackInfo ci) {
		((EntityAccess) entity).postTick(); // not vanilla?
	}

	@Inject (method = "init", at = @At (value = "INVOKE", target = "Lnet/minecraft/tag/Tag;values()Ljava/util/Collection;"), cancellable = true)
	private void fukkit_setSpawnPoint(LevelInfo levelInfo, CallbackInfo ci) {
		if (this.generator != null) {
			Random random = new Random(this.getSeed());
			Location location = this.generator.getFixedSpawnLocation(((WorldAccess) this).getBukkit(), random);
			if (location != null) {
				if (location.getWorld() != ((WorldAccess) this).getBukkit()) {
					throw new IllegalStateException("Cannot set spawn point for " + this.properties.getLevelName() + " at " + location);
				} else {
					this.properties.setSpawnPos(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
					ci.cancel();
				}
			}
		}
	}

	@Inject (method = "save", at = @At (value = "JUMP", opcode = Opcodes.IFNULL, ordinal = 0))
	private void fukkit_saveEvent(ProgressListener progressListener, boolean flush, boolean bl, CallbackInfo ci) {
		Bukkit.getPluginManager().callEvent(new WorldSaveEvent(((WorldAccess) this).getBukkit()));
	}

	@Inject (method = "save", at = @At ("TAIL"))
	private void fukkit_saveChunks(ProgressListener progressListener, boolean flush, boolean bl, CallbackInfo ci) {
		LevelProperties properties = this.getLevelProperties();
		this.getWorldBorder().save(properties);
		properties.setCustomBossEvents(this.server.getBossBarManager().toTag());
		this.getSaveHandler().saveWorld(properties, this.server.getPlayerManager().getUserData());
	}

	@Redirect (method = "getMobCountsByCategory", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;isPersistent()Z"))
	private boolean fukkit_canImmediatelyDespawn(MobEntity entity) {
		return entity.canImmediatelyDespawn(0) && entity.isPersistent();
	}

	@Redirect (method = "getMobCountsByCategory", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;cannotDespawn()Z"))
	private boolean fukkit_voidCall(MobEntity entity) {return false;}
	// TODO finish

	@Redirect (method = "spawnEntity", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;addEntity(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_addEntity(ServerWorld world, Entity entity) {
		return this.addEntity0(entity, CreatureSpawnEvent.SpawnReason.DEFAULT);
	}

	public boolean fukkit$addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
		return this.addEntity0(entity, reason);
	}

	public boolean fukkit$addEntitySerialized(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
		return this.addEntity0(entity, reason);
	}

	// CraftBukkit start
	private boolean addEntity0(Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
		if (entity.removed) {
			// WorldServer.LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityTypes.getName(entity.getEntityType())); // CraftBukkit
			return false;
		} else if (this.checkUuid(entity)) {
			return false;
		} else {
			if (!CraftEventFactory.doEntityAddEventCalling((World) (Object) this, entity, spawnReason)) {
				return false;
			}
			// CraftBukkit end
			Chunk chunk = this.getChunk(MathHelper.floor(entity.getX() / 16.0D), MathHelper.floor(entity.getZ() / 16.0D), ChunkStatus.FULL, entity.teleporting);

			if (!(chunk instanceof WorldChunk)) {
				return false;
			} else {
				chunk.addEntity(entity);
				this.loadEntityUnchecked(entity);
				return true;
			}
		}
	}

	@Redirect (method = "checkUuid", at = @At (value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
	private void fukkit_suppress(Logger logger, String message, Object p0, Object p1) {}

	@Redirect (method = "unloadEntity", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getNavigation()Lnet/minecraft/entity/ai/pathing/EntityNavigation;"))
	private EntityNavigation fukkit_spigot_5278(MobEntity entity) {
		if (entity instanceof DrownedEntity) {

			this.entityNavigations.remove(((DrownedEntityAccess) entity).getWaterNavigation());
			((EntityAccess) entity).setValid(false);
			return ((DrownedEntityAccess) entity).getLandNavigation();
		} else {
			((EntityAccess) entity).setValid(false);
			return entity.getNavigation();
		}

	}

	@Redirect (method = "loadEntityUnchecked", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getNavigation()Lnet/minecraft/entity/ai/pathing/EntityNavigation;"))
	private EntityNavigation fukkit_spigot_5278_2(MobEntity entity) {
		if (entity instanceof DrownedEntity) {
			this.entityNavigations.remove(((DrownedEntityAccess) entity).getWaterNavigation());
			((EntityAccess) entity).setValid(true);
			return ((DrownedEntityAccess) entity).getLandNavigation();
		} else {
			((EntityAccess) entity).setValid(true);
			return entity.getNavigation();
		}
	}

	public void fukkit$strikeLightning(LightningEntity lightning, LightningStrikeEvent.Cause cause) {
		LightningStrikeEvent event = new LightningStrikeEvent((org.bukkit.World) this.fukkit$getBukkit(), (org.bukkit.entity.LightningStrike) ((EntityAccess) lightning).getBukkit(), cause);
		this.fukkit$getBukkitServer().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			return;
		}
		// CraftBukkit end
		this.globalEntities.add(lightning);
		this.server.getPlayerManager().sendToAround(null, lightning.getX(), lightning.getY(), lightning.getZ(), 512.0D, this.dimension.getType(), new EntitySpawnGlobalS2CPacket(lightning));
	}

	private static final ThreadLocal<ServerPlayerEntity> PLAYER = new ThreadLocal<>();

	@Inject (method = "setBlockBreakingInfo", at = @At (value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_getPlayer(int entityId, BlockPos pos, int progress, CallbackInfo ci, Iterator iterator, ServerPlayerEntity entity, double d) {
		PLAYER.set(entity);
	}

	@ModifyVariable (method = "setBlockBreakingInfo", at = @At (value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I"), index = 6, ordinal = 0)
	public double fukkit_cancelSomething(double x, int entityId, BlockPos pos, int progress) {
		Entity entity = this.getEntityById(entityId);
		if (entity instanceof ServerPlayerEntity && !((ServerPlayerEntityAccess) PLAYER.get()).getBukkit().canSee(((ServerPlayerEntityAccess) entity).getBukkit())) {
			return Double.NEGATIVE_INFINITY; // hahayes infinity iq time
		} else return x;
	}

	private Explosion explosion;

	@Inject (method = "createExplosion", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V"))
	private void fukkit_check(Entity entity, DamageSource damageSource, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, CallbackInfoReturnable<Explosion> cir) {
		if (((ExplosionAccess) this.explosion).wasCancelled()) cir.setReturnValue(this.explosion);
		this.explosion = null; // gc
	}

	@Redirect (method = "createExplosion", at = @At (value = "NEW", target = "net/minecraft/world/explosion/Explosion"))
	private Explosion fukkit_super(World world, Entity entity, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType blockDestructionType, Entity entity0, DamageSource damageSource, double x0, double y0, double z0, float power0, boolean createFire0, Explosion.DestructionType destructionType) {
		return this.explosion = super.createExplosion(entity, damageSource, x, y, z, power, createFire, blockDestructionType);
	}

	@Redirect (method = "createExplosion", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;setDamageSource(Lnet/minecraft/entity/damage/DamageSource;)V"))
	private void fukkit_remove(Explosion explosion, DamageSource damageSource) {}

	@Redirect (method = "createExplosion", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V"))
	private void fukkit_remove(Explosion explosion) {}

	@Redirect (method = "createExplosion", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;affectWorld(Z)V"))
	private void fukkit_remove(Explosion explosion, boolean bl) {}

	/**
	 * @author HalfOf2
	 * @reason redirect method
	 * todo replace with duplicate inject
	 */
	@Overwrite
	public <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
		return this.fukkit$spawnParticles(null, particle, x, y, z, count, deltaX, deltaY, deltaZ, speed, false);
	}

	public <T extends ParticleEffect> int fukkit$spawnParticles(ServerPlayerEntity entity, T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
		ParticleS2CPacket particlePacket = new ParticleS2CPacket(particle, force, x, y, z, (float) deltaX, (float) deltaY, (float) deltaZ, (float) speed, count);
		int players = 0;
		for (ServerPlayerEntity player : this.players) {
			if (entity != null && !((ServerPlayerEntityAccess) entity).getBukkit().canSee(((ServerPlayerEntityAccess) entity).getBukkit()))
				continue; // CraftBukkit
			if (this.sendToPlayerIfNearby(player, force, x, y, z, particlePacket)) { // CraftBukkit
				++players;
			}
		}
		return players;
	}

	@Inject(method = "method_17989(Ljava/lang/String;)Lnet/minecraft/item/map/MapState;", at = @At("RETURN"))
	private static void fukkit_initializeEvent(String string, CallbackInfoReturnable<MapState> stateReturn) {
		MapInitializeEvent event = new MapInitializeEvent(((MapStateAccess)stateReturn.getReturnValue()).getMapView());
		Bukkit.getServer().getPluginManager().callEvent(event);
	}
}