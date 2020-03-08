package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.player;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.LivingEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.damage.DamageSourceAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.LivingEntityMixin;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.container.Container;
import net.minecraft.container.PlayerContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.*;
import net.minecraft.world.biome.Biomes;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.List;

@Implements (@Interface (iface = PlayerEntityAccess.class, prefix = "fukkit$"))
@Mixin (PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
	@Shadow
	protected abstract void dropShoulderEntities();

	@Shadow
	public abstract String getEntityName();

	@Shadow
	public abstract void addExhaustion(float exhaustion);

	@Shadow
	public abstract void increaseStat(Stat<?> stat, int amount);

	@Shadow
	public abstract void increaseStat(Identifier stat, int amount);

	@Shadow
	public abstract BlockPos getSpawnPosition();

	@Shadow public abstract void setPlayerSpawn(BlockPos blockPos, boolean bl, boolean bl2);

	@Shadow public abstract boolean isCreative();

	@Shadow protected abstract boolean isWithinSleepingRange(BlockPos sleepPos, Direction direction);

	@Shadow protected abstract boolean isBedObstructed(BlockPos pos, Direction direction);

	@Shadow private int sleepTimer;

	@Override
	@Shadow public abstract void sleep(BlockPos pos);

	@Shadow public abstract CompoundTag getShoulderEntityLeft();

	@Shadow protected abstract void setShoulderEntityLeft(CompoundTag entityTag);

	@Shadow public abstract CompoundTag getShoulderEntityRight();

	@Shadow protected abstract void setShoulderEntityRight(CompoundTag entityTag);

	@Shadow public int experienceLevel;
	@Shadow @Final public PlayerInventory inventory;
	@Shadow @Final public PlayerContainer playerContainer;
	@Shadow public Container container;
	@Shadow @Final public PlayerAbilities abilities;

	@Shadow public abstract void incrementStat(Stat<?> stat);

	@Shadow public abstract void incrementStat(Identifier stat);

	@Shadow public abstract Arm getMainArm();

	@Shadow public float experienceProgress;
	@Shadow public int totalExperience;

	@Shadow protected native boolean isImmobile();

	@Shadow public abstract Scoreboard getScoreboard();

	@Shadow protected HungerManager hungerManager;
	// TODO add *this* to EnderChestInventory/HungerManager constructor
	protected boolean fauxSleeping;
	protected String spawnWorld;
	protected int oldLevel = -1;

	public CraftPlayer getBukkitEntity(){
		return (CraftPlayer) this.fukkit$getBukkit();
	}

	@Redirect (method = "updateTurtleHelmet", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
	private boolean fukkit_addEffect(PlayerEntity entity, StatusEffectInstance effect) {
		return ((LivingEntityAccess) this).addEffect(effect, EntityPotionEffectEvent.Cause.TURTLE_HELMET);
	}

	@Redirect (method = "tickMovement", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"))
	private void fukkit_addEffect(PlayerEntity entity, float amount) {
		((LivingEntityAccess) this).heal(amount, EntityRegainHealthEvent.RegainReason.REGEN);
	}

	@Inject (method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At (value = "RETURN", ordinal = 1))
	private void fukkit_dropItemEvent(ItemStack stack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {
		HumanEntity player = ((PlayerEntityAccess<?>) this).getBukkit();
		ItemEntity entity = cir.getReturnValue();
		Item item = ((EntityAccess<CraftItem>) entity).getBukkit();
		// technically this should be wrapped in an instanceof player
		PlayerDropItemEvent event = new PlayerDropItemEvent((Player) player, item);
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			org.bukkit.inventory.ItemStack cur = player.getInventory().getItemInHand();
			if (bl2 && (cur == null || cur.getAmount() == 0)) {
				// The complete stack was dropped
				player.getInventory().setItemInHand(item.getItemStack());
			} else if (bl2 && cur.isSimilar(item.getItemStack()) && cur.getAmount() < cur.getMaxStackSize() && item.getItemStack().getAmount() == 1) {
				// Only one item is dropped
				cur.setAmount(cur.getAmount() + 1);
				player.getInventory().setItemInHand(cur);
			} else {
				// Fallback
				player.getInventory().addItem(item.getItemStack());
			}
			cir.setReturnValue(null);
		}
	}

	@Inject (method = "readCustomDataFromTag", at = @At (value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z"), slice = @Slice (from = @At (value = "CONSTANT", args = "stringValue=Score"), to = @At (value = "CONSTANT:FIRST", args = "stringValue=SpawnY")))
	private void fukkit_spawnWorld(CompoundTag tag, CallbackInfo ci) {
		this.spawnWorld = tag.getString("SpawnWorld");
		if ("".equals(this.spawnWorld)) {
			this.spawnWorld = ((WorldAccess) this.world).getBukkitServer().getWorlds().get(0).getName();
		}
	}

	@Inject (method = "writeCustomDataToTag", at = @At ("TAIL"))
	private void fukkit_fixBedMultiWorld(CompoundTag tag, CallbackInfo ci) {
		tag.putString("SpawnWorld", this.spawnWorld);
	}

	@Inject (method = "damage", at = @At (value = "RETURN", ordinal = 1))
	private void fukkit_invulnTakeKnock(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		((PlayerEntityAccess<?>) this).setForceExplosionKnockback(true);
	}

	@Redirect (method = "damage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;dropShoulderEntities()V"))
	private void fukkit_movedDown(PlayerEntity entity) {}

	@Inject (method = "damage", at = @At (value = "JUMP", ordinal = 0, opcode = Opcodes.IF_ACMPNE, shift = At.Shift.AFTER, by = 1), slice = @Slice (from = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/Difficulty;", ordinal = 0)), cancellable = true)
	private void fukkit_returnFalse(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	@Inject (method = "damage", at = @At ("RETURN"), slice = @Slice (from = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/Difficulty;", ordinal = 2), to = @At ("TAIL")), cancellable = true)
	private void fukkit_dontIgnore(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		boolean damaged = super.damage(source, amount);
		if (damaged) this.dropShoulderEntities();
		cir.setReturnValue(damaged);
	}

	/**
	 * the entire method is yeeted, none of the original logic remains
	 *
	 * @reason md5pls
	 * @author HalfOf2
	 */
	@Overwrite
	public boolean shouldDamagePlayer(PlayerEntity entity) {
		// CraftBukkit start - Change to check OTHER player's scoreboard team according to API
		// To summarize this method's logic, it's "Can parameter hurt this"
		org.bukkit.scoreboard.Team team;
		if (entity instanceof ServerPlayerEntity) {
			ServerPlayerEntityAccess other = (ServerPlayerEntityAccess) entity;
			team = other.getBukkit().getScoreboard().getPlayerTeam(other.getBukkit());
			if (team == null || team.allowFriendlyFire()) {
				return true;
			}
		} else {
			// This should never be called, but is implemented anyway
			org.bukkit.OfflinePlayer thisPlayer = ((WorldAccess) entity.world).getBukkitServer().getOfflinePlayer(entity.getEntityName());
			team = ((WorldAccess) entity.world).getBukkitServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(thisPlayer);
			if (team == null || team.allowFriendlyFire()) {
				return true;
			}
		}

		if ((Object) this instanceof ServerPlayerEntity) {
			return !team.hasPlayer(((ServerPlayerEntityAccess) this).getBukkit());
		}
		return !team.hasPlayer(((WorldAccess) this.world).getBukkitServer().getOfflinePlayer(this.getEntityName()));
		// CraftBukkit end
	}

	/**
	 * todo replace with injects after you verify you've called the correct method everwhere
	 *
	 * @author HalfOf2
	 * @reason method signature change
	 */
	@Override
	@Overwrite
	public void applyDamage(DamageSource source, float f) {
		throw new IllegalStateException("Half you absolute moron u called the wrong method");
	}

	private int duration;

	@Redirect (method = "attack", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isOnFire()Z"))
	private boolean fukkit_hijackIf(Entity entity) {
		if (!entity.isOnFire()) {
			EntityCombustByEntityEvent event = new EntityCombustByEntityEvent((org.bukkit.entity.Entity) this.fukkit$getBukkit(), ((EntityAccess<?>) entity).getBukkit(), 1);
			Bukkit.getPluginManager().callEvent(event);
			this.duration = event.getDuration();
			return event.isCancelled(); // continue if
		}
		return true;
	}

	@Redirect (method = "attack", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V", ordinal = 0))
	private void fukkit_eventDuration(Entity entity, int seconds) {
		((EntityAccess<?>) entity).setOnFireFor(this.duration, false);
	}

	// not vanilla?
	@Redirect (method = "attack", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean fukkit_onlyApply(LivingEntity entity, DamageSource source, float amount) {
		if (entity.damage(((DamageSourceAccess) source).sweep(), amount)) {
			entity.takeKnockback((LivingEntity) (Object) this, 0.4F, MathHelper.sin(this.yaw * 0.017453292F), -MathHelper.cos(this.yaw * 0.017453292F));
		}
		return false;
	}

	@Redirect (method = "attack", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(Lnet/minecraft/entity/Entity;FDD)V", ordinal = 1))
	private void fukkit_replaced(LivingEntity entity, Entity attacker, float speed, double xMovement, double zMovement) {

	}

	// assert main thread
	private Vec3d vec3d;

	@Inject (method = "attack", at = @At (value = "OPCODE", opcode = Opcodes.INSTANCEOF, args = "desc=net/minecraft/server/network/ServerPlayerEntity"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_playerVeloEvent(Entity target, CallbackInfo ci, float f, float h, boolean bl, boolean bl2, int j, boolean bl3, boolean bl4, float k, boolean bl5, int l, Vec3d vec3d) {
		this.vec3d = vec3d;
	}

	@Redirect (method = "attack", at = @At (value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/Entity;velocityModified:Z", ordinal = 0))
	private boolean fukkit_modified(Entity entity) {
		// check main thread
		if (entity.velocityModified) {
			Player player = ((ServerPlayerEntityAccess) entity).getBukkit();
			org.bukkit.util.Vector vector = CraftVector.toBukkit(this.vec3d);
			PlayerVelocityEvent event = new PlayerVelocityEvent(player, vector.clone());
			((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return false; // exit if
			} else if (!vector.equals(event.getVelocity())) {
				player.setVelocity(vector);
				return true; // continue if
			}
		}
		return false; // exit if
	}

	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V", ordinal = 1))
	private void fukkit_entityCombustEvent(Entity entity, int seconds) {
		EntityCombustByEntityEvent event = new EntityCombustByEntityEvent((org.bukkit.entity.Entity) this.fukkit$getBukkit(), ((EntityAccess<?>)entity).getBukkit(), seconds);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			((EntityAccess<?>) entity).setOnFireFor(event.getDuration(), false);
		}
	}

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;extinguish()V"))
	private void fukkit_resync(Entity target, CallbackInfo ci) {
		if((Object)this instanceof ServerPlayerEntity) {
			((ServerPlayerEntityAccess)this).getBukkit().updateInventory();
		}
	}

	/**
	 * todo replace with injects
	 * @author HalfOf2
	 * @reason method signature change
	 */
	@Overwrite
	public Either<PlayerEntity.SleepFailureReason, Unit> trySleep(BlockPos pos) {
		return this.fukkit$sleep(pos, false);
	}

	// accessor needed?
	@Unique
	private Either<PlayerEntity.SleepFailureReason, Unit> getBedResult(BlockPos blockposition, Direction enumdirection) {
		if (!this.world.isClient) {
			if (this.isSleeping() || !this.isAlive()) {
				return Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM);
			}

			// CraftBukkit - moved world and biome check from BlockBed interact handling here
			if (!this.world.dimension.canPlayersSleep() || this.world.getBiome(blockposition) == Biomes.NETHER || !this.world.dimension.hasVisibleSky()) {
				return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_HERE);
			}

			if (this.world.isDay()) {
				this.setPlayerSpawn(blockposition, false, true);
				return Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_NOW);
			}

			if (!this.isWithinSleepingRange(blockposition, enumdirection)) {
				return Either.left(PlayerEntity.SleepFailureReason.TOO_FAR_AWAY);
			}

			if (this.isBedObstructed(blockposition, enumdirection)) {
				return Either.left(PlayerEntity.SleepFailureReason.OBSTRUCTED);
			}

			if (!this.isCreative()) {
				List<HostileEntity> list = this.world.getEntities(HostileEntity.class, new Box((double) blockposition.getX() - 8.0D, (double) blockposition.getY() - 5.0D, (double) blockposition.getZ() - 8.0D, (double) blockposition.getX() + 8.0D, (double) blockposition.getY() + 5.0D, (double) blockposition.getZ() + 8.0D), (hostile) -> hostile.isAngryAt((PlayerEntity) (Object) this));
				if (!list.isEmpty()) {
					return Either.left(PlayerEntity.SleepFailureReason.NOT_SAFE);
				}
			}
		}
		return Either.right(Unit.INSTANCE);
	}

	public Either<PlayerEntity.SleepFailureReason, Unit> fukkit$sleep(BlockPos blockposition, boolean force) {
		Direction direction = this.world.getBlockState(blockposition).get(HorizontalFacingBlock.FACING);
		Either<PlayerEntity.SleepFailureReason, Unit> bedResult = this.getBedResult(blockposition, direction);

		if (bedResult.left().orElse(null) == PlayerEntity.SleepFailureReason.OTHER_PROBLEM) {
			return bedResult; // return immediately if the result is not bypassable by plugins
		}

		if (force) {
			bedResult = Either.right(Unit.INSTANCE);
		}

		if (this.fukkit$getBukkit() instanceof Player) {
			bedResult = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerBedEnterEvent((PlayerEntity) (Object) this, blockposition, bedResult);

			if (bedResult.left().isPresent()) {
				return bedResult;
			}
		}
		// CraftBukkit end

		this.sleep(blockposition);
		this.sleepTimer = 0;
		if (this.world instanceof ServerWorld) {
			((ServerWorld) this.world).updatePlayersSleeping();
		}

		return Either.right(Unit.INSTANCE);
	}

	// replace with assert main, and verify recursion
	private BlockPos bedPos;
	@Inject(method = "wakeUp(ZZ)V", at = @At("HEAD"))
	private void fukkit_bedPos(boolean bl, boolean bl2, CallbackInfo ci) {
		this.bedPos = this.getSleepingPosition().orElse(null);
	}

	@Inject(method = "wakeUp(ZZ)V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;sleepTimer:I"))
	private void fukkit_playerLeaveBedEvent(boolean bl, boolean bl2, CallbackInfo ci) {
		if(this.fukkit$getBukkit() instanceof Player) {
			Player player = (Player) this.fukkit$getBukkit();
			Block bed;
			World bukkit = ((WorldAccess)this.world).getBukkit();
			if(this.bedPos != null) {
				bed = bukkit.getBlockAt(this.bedPos.getX(), this.bedPos.getY(), this.bedPos.getZ());
			} else {
				bed = bukkit.getBlockAt(player.getLocation());
			}

			PlayerBedLeaveEvent event = new PlayerBedLeaveEvent(player, bed, true);
			((WorldAccess)this.world).getBukkitServer().getPluginManager().callEvent(event);
		}
		this.bedPos = null; // reset
	}

	@Inject(method = "setPlayerSpawn", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;spawnForced:Z", ordinal = 0))
	private void fukkit_spawnWorld(BlockPos blockPos, boolean bl, boolean bl2, CallbackInfo ci) {
		this.spawnWorld = this.world.getLevelProperties().getLevelName();
	}

	@Inject(method = "setPlayerSpawn", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;spawnForced:Z", ordinal = 1))
	private void fukkit_spawnWorld0(BlockPos blockPos, boolean bl, boolean bl2, CallbackInfo ci) {
		this.spawnWorld = "";
	}

	@Redirect(method = "method_23669", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setFlag(IZ)V"))
	private void fukkit_toggleGlideEvent(PlayerEntity entity, int index, boolean value) {
		if(CraftEventFactory.callToggleGlideEvent(entity, true).isCancelled())
			this.setFlag(7, true);
		else {
			this.setFlag(7, true);
			this.setFlag(7, false);
		}
	}

	@Inject(method = "method_23670", at = @At("HEAD"))
	private void fukkit_toggleGlideEvent(CallbackInfo ci) {
		if(CraftEventFactory.callToggleGlideEvent((PlayerEntity) (Object) this, false).isCancelled())
			ci.cancel();
	}

	// two birds 1 stone
	@Redirect(method = "dropShoulderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;method_7296(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void fukkit_voidCalls(PlayerEntity entity, CompoundTag compoundTag) {}

	@Redirect(method = "dropShoulderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setShoulderEntityLeft(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void fukkit_setLeft(PlayerEntity entity, CompoundTag entityTag) {
		if(this.spawnEntityFromShoulder(this.getShoulderEntityLeft())) {
			this.setShoulderEntityLeft(entityTag);
		}
	}

	@Redirect(method = "dropShoulderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setShoulderEntityRight(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void fukkit_setRight(PlayerEntity entity, CompoundTag entityTag) {
		if(this.spawnEntityFromShoulder(this.getShoulderEntityRight())) {
			this.setShoulderEntityRight(entityTag);
		}
	}

	@Unique
	private boolean spawnEntityFromShoulder(CompoundTag nbttagcompound) { // CraftBukkit void->boolean
		if (!this.world.isClient && !nbttagcompound.isEmpty()) {
			return EntityType.getEntityFromTag(nbttagcompound, this.world).map((entity) -> { // CraftBukkit
				if (entity instanceof TameableEntity) {
					((TameableEntity) entity).setOwnerUuid(this.uuid);
				}

				entity.updatePosition(this.getX(), this.getY() + 0.699999988079071D, this.getZ());
				return ((ServerWorldAccess) this.world).addEntitySerialized(entity, CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY); // CraftBukkit
			}).orElse(true); // CraftBukkit
		}

		return true; // CraftBukkit
	}
	public boolean fukkit$isFauxSleeping() {
		return this.fauxSleeping;
	}

	public void fukkit$setFauxSleeping(boolean fauxSleeping) {
		this.fauxSleeping = fauxSleeping;
	}

	public String fukkit$getSpawnWorld() {
		return this.spawnWorld;
	}

	public void fukkit$setSpawnWorld(String world) {
		this.spawnWorld = world;
	}

	public int fukkit$getOldLevel() {
		return this.oldLevel;
	}

	public void fukkit$setOldLevel(int level) {
		this.oldLevel = level;
	}
}
