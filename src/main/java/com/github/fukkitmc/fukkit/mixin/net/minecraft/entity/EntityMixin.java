package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.LivingEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.mob.MobEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.PortalForcerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldSaveHandlerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.entity.EntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Implements (@Interface (iface = EntityAccess.class, prefix = "fukkit$"))
@Mixin (Entity.class)
public abstract class EntityMixin {
	@Shadow
	public float yaw;
	@Shadow
	public World world;
	@Shadow
	private double x;
	@Shadow
	private double z;

	@Shadow
	public abstract EntityPose getPose();

	@Shadow
	protected abstract void tickNetherPortal();

	@Shadow
	private int fireTicks;
	@Shadow
	public boolean horizontalCollision;
	@Shadow
	private double y;

	@Shadow
	public abstract boolean isFireImmune();

	@Shadow
	public abstract boolean damage(DamageSource source, float amount);

	@Shadow
	public abstract void remove();

	@Shadow
	public abstract void populateCrashReport(CrashReportSection section);

	@Shadow
	public native void setWorld(World world);

	@Shadow
	protected abstract boolean canAddPassenger(Entity passenger);

	@Shadow
	private Entity vehicle;
	@Shadow
	@Final
	private List<Entity> passengerList;
	@Shadow
	protected int ridingCooldown;

	@Shadow
	public abstract boolean isSwimming();

	@Shadow
	@Final
	private static TrackedData<Integer> AIR;
	@Shadow
	public boolean removed;

	@Shadow
	public abstract MinecraftServer getServer();

	@Shadow
	public DimensionType dimension;

	@Shadow
	public abstract Vec3d getVelocity();

	@Shadow
	public abstract double getX();

	@Shadow
	public abstract double getZ();

	@Shadow
	public abstract Vec3d getLastNetherPortalDirectionVector();

	@Shadow
	public abstract Direction getLastNetherPortalDirection();

	@Shadow
	public abstract double getY();

	@Shadow
	public abstract void detach();

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow @Final protected DataTracker dataTracker;

	@Shadow public abstract ItemEntity dropItem(ItemConvertible item);

	@Shadow public abstract boolean isInvulnerableTo(DamageSource damageSource);

	@Shadow @Final protected Random random;

	@Shadow protected abstract boolean getFlag(int index);

	@Shadow protected abstract void setFlag(int index, boolean value);

	@Shadow public abstract void requestTeleport(double destX, double destY, double destZ);

	@Shadow public abstract Box getBoundingBox();

	@Shadow public abstract Entity getVehicle();

	@Shadow public abstract void updatePosition(double x, double y, double z);

	@Shadow protected UUID uuid;
	@Shadow public float pitch;

	@Shadow public abstract Vec3d getPos();

	@Shadow public abstract int getEntityId();

	@Shadow public abstract void setVelocity(Vec3d velocity);

	@Shadow protected boolean inLava;
	@Shadow public float fallDistance;

	@Shadow public abstract void setFireTicks(int ticks);

	@Shadow public abstract void refreshPositionAndAngles(BlockPos pos, float yaw, float pitch);

	@Shadow public abstract void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch);

	private static final int CURRENT_LEVEL = 2;
	private CraftEntity entity;
	public boolean persist = true;
	public boolean valid;
	public org.bukkit.projectiles.ProjectileSource projectileSource; // For projectiles only
	public boolean forceExplosionKnockback; // SPIGOT-949

	@Inject (method = "setPose", at = @At ("HEAD"), cancellable = true)
	public void pose(EntityPose pose, CallbackInfo ci) {
		if (pose == this.getPose()) {
			ci.cancel();
			return;
		}
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(new EntityPoseChangeEvent((org.bukkit.entity.Entity) this.fukkit$getBukkit(), Pose.values()[pose.ordinal()]));
	}

	@Inject (method = "updatePosition", at = @At ("RETURN"))
	private void fukkit_checkChunk(double x, double y, double z, CallbackInfo ci) {
		if (this.valid) ((ServerWorld) this.world).checkChunk((Entity) (Object) this);
	}

	public void fukkit$postTick() {
		if (!(((Entity) (Object) this) instanceof ServerPlayerEntity)) this.tickNetherPortal();
	}

	@Redirect (method = "baseTick", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickNetherPortal()V"))
	private void tickPortal(Entity entity) {
		this.fukkit$postTick();
	}

	/**
	 * @reason redirection of call, I could duplicate the code but this class is already long enough :tiny_potato:
	 * @author HalfOf2
	 */
	@Overwrite
	public void setOnFireFor(int seconds) {
		this.fukkit$setOnFireFor(seconds, true);
	}

	@Inject (method = "setOnFireFromLava", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V"))
	private void fukkit_entityCombustEvent(CallbackInfo ci) {
		if (((Entity) (Object) this) instanceof LivingEntity && this.fireTicks <= 0) {
			Block damager = null;
			org.bukkit.entity.Entity damaged = (org.bukkit.entity.Entity) this.fukkit$getBukkit();
			EntityCombustByBlockEvent event = new EntityCombustByBlockEvent(damager, damaged, 15);
			((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);

			if (!event.isCancelled()) this.fukkit$setOnFireFor(event.getDuration(), false);
		} else this.fukkit$setOnFireFor(15, false);
	}

	public void fukkit$setOnFireFor(int seconds, boolean callEvent) {
		if (callEvent) {
			EntityCombustEvent event = new EntityCombustEvent((org.bukkit.entity.Entity) this.fukkit$getBukkit(), seconds);
			((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return;
			}

			seconds = event.getDuration();
		}
		int ticks = seconds * 20;
		if (((Entity) (Object) this) instanceof LivingEntity) {
			ticks = ProtectionEnchantment.transformFireDuration((LivingEntity) (Object) this, ticks);
		}

		if (this.fireTicks < ticks) {
			this.fireTicks = ticks;
		}
	}

	@Inject (method = "move", at = @At (value = "FIELD", ordinal = 2, target = "Lnet/minecraft/entity/Entity;onGround:Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_vehicleCollideEvent(MovementType type, Vec3d movement, CallbackInfo ci, Vec3d vel, BlockPos blockPos, BlockState blockState, net.minecraft.block.Block block) {
		Object entity;
		if (this.horizontalCollision && (entity = this.fukkit$getBukkit()) instanceof Vehicle) {
			Vehicle vehicle = (Vehicle) entity;
			Block collision = ((WorldAccess) this.world).getBukkit().getBlockAt(MathHelper.floor(this.x), MathHelper.floor(this.y), MathHelper.floor(this.z));
			if (movement.x > vel.x) {
				collision = collision.getRelative(BlockFace.EAST);
			} else if (movement.x < vel.x) {
				collision = collision.getRelative(BlockFace.WEST);
			} else if (movement.z > vel.z) {
				collision = collision.getRelative(BlockFace.SOUTH);
			} else if (movement.z < vel.z) {
				collision = collision.getRelative(BlockFace.NORTH);
			}

			if (!collision.getType().isAir()) {
				VehicleBlockCollisionEvent event = new VehicleBlockCollisionEvent(vehicle, collision);
				((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);
			}
		}
	}

	@Redirect (method = "move", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V"))
	private void fukkit_combustEvent(Entity entity, int seconds) {
		EntityCombustByBlockEvent event = new EntityCombustByBlockEvent(null, (org.bukkit.entity.Entity) this.fukkit$getBukkit(), seconds);
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) this.fukkit$setOnFireFor(event.getDuration(), false);
	}

	@Inject (method = "moveToBoundingBoxCenter", at = @At ("RETURN"))
	private void fukkit_validateMove(CallbackInfo ci) {
		if (this.valid) ((ServerWorld) this.world).checkChunk((Entity) (Object) this);
	}

	@Redirect (method = "setOnFireFromLava", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V"))
	private void fukkit_voidCall(Entity entity, int seconds) {}

	protected void burn(float time) {
		if (!this.isFireImmune()) {
			this.damage(DamageSource.IN_FIRE, time);
		}
	}

	@Inject (method = "setWorld", at = @At ("HEAD"))
	private void fukkit_nullCheck(World world, CallbackInfo ci) {
		if (world == null) {
			this.remove();
			this.world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
			ci.cancel();
		}
	}

	// TODO check if necessary, might need to remove, lithium would make this redundant if it's an optimization
	// should not affect game mechanics, but it's worth considering
	@Inject (method = "updatePositionAndAngles", at = @At ("RETURN"))
	private void fukkit_cacheChunk(double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
		this.world.getChunk((int) Math.floor(this.x) >> 4, (int) Math.floor(this.z) >> 4);
	}

	@Inject (method = "saveSelfToTag", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getSavedEntityId()Ljava/lang/String;", shift = At.Shift.AFTER))
	private void fukkit_persistFlag(CompoundTag tag, CallbackInfoReturnable<Boolean> cir) {
		if (!this.persist) cir.setReturnValue(false);
	}

	@Redirect (method = "toTag", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;getRawId()I"))
	private int fukkit_idCompat(DimensionType type) {
		return ((DimensionTypeAccess) type).getType().getRawId();
	}

	@Inject (method = "toTag", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getCustomName()Lnet/minecraft/text/Text;", ordinal = 0))
	private void fukkit_1_8_compat(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		UUID uuid = ((WorldSaveHandlerAccess) ((ServerWorld) this.world).getSaveHandler()).getUUID();
		tag.putLong("WorldUUIDLeast", uuid.getLeastSignificantBits());
		tag.putLong("WorldUUIDMost", uuid.getMostSignificantBits());
		tag.putInt("Bukkit.updateLevel", CURRENT_LEVEL);
	}

	@Inject (method = "toTag", at = @At ("RETURN"))
	private void fukkit_storeBukkit(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (this.entity != null) this.entity.storeBukkitValues(tag);
	}

	@Inject (method = "fromTag", at = @At (value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;)Z", ordinal = 0))
	private void fukkit_redundant(CompoundTag tag, CallbackInfo ci) {}

	@Inject (method = "fromTag", at = @At (value = "RETURN"))
	private void fukkit_resetWorld_andStuff(CompoundTag tag, CallbackInfo ci) {
		try {
			if (((Entity) (Object) this) instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) (Object) this;
				if (entity instanceof TameableEntity && !EntityUtil.isLevelAtLeast(tag, 2) && !tag.getBoolean("PersistenceRequired")) {
					MobEntity mob = (MobEntity) entity;
					((MobEntityAccess) mob).setPersistent(!mob.canImmediatelyDespawn(0));
				}
			}

			if (((Entity) (Object) this) instanceof ServerPlayerEntity) {
				Server server = Bukkit.getServer();
				org.bukkit.World bukkit;
				String worldName = tag.getString("world");
				if (tag.contains("WorldUUIDMost") && tag.contains("WorldUUIDLeast")) {
					UUID uid = new UUID(tag.getLong("WorldUUIDMost"), tag.getLong("WorldUUIDLeast"));
					bukkit = server.getWorld(uid);
				} else {
					bukkit = server.getWorld(worldName);
				}

				if (bukkit == null) {
					bukkit = ((WorldAccess) ((org.bukkit.craftbukkit.CraftServer) server).getServer().getWorld(DimensionType.OVERWORLD)).getBukkit();
				}
				this.setWorld(bukkit == null ? null : ((CraftWorld) bukkit).getHandle());
			}

			((CraftEntity) this.fukkit$getBukkit()).readBukkitValues(tag);
		} catch (Throwable t) {
			CrashReport crashReport = CrashReport.create(t, "Loading entity NBT");
			CrashReportSection crashReportSection = crashReport.addElement("Entity being loaded");
			this.populateCrashReport(crashReportSection);
			throw new CrashException(crashReport);
		}
	}

	@Inject (method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At (value = "NEW", target = "net/minecraft/entity/ItemEntity"),cancellable=true)
	private void fukkit_captureDrops(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
		if (((Entity) (Object) this) instanceof LivingEntity && !((LivingEntityAccess) this).shouldForceDrops()) {
			((LivingEntityAccess) this).getDrops().add(CraftItemStack.asBukkitCopy(stack));
			cir.setReturnValue(null);
		}
	}

	@Inject (method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_dropEvent(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir, ItemEntity entity) {
		EntityDropItemEvent event = new EntityDropItemEvent((org.bukkit.entity.Entity) this.fukkit$getBukkit(), (Item) ((EntityAccess) entity).getBukkit());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) cir.setReturnValue(null);
	}

	@Redirect (method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addPassenger(Lnet/minecraft/entity/Entity;)V"))
	private void fukkit_addPassanger(Entity entity, Entity this_) {
		if (!this.canAddPassenger(this_)) this.vehicle = null;
	}

	@Redirect (method = "stopRiding", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;removePassenger(Lnet/minecraft/entity/Entity;)V"))
	private void fukkit_stopRiding(Entity entity, Entity this_) {
		if (!this.remPassenger(this_)) this.vehicle = entity;
	}

	protected boolean remPassenger(Entity entity) {
		if (entity.getVehicle() == (Object) this) {
			throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
		} else {
			CraftEntity craft = (CraftEntity) ((EntityAccess<?>) entity).getBukkit().getVehicle();
			Entity orig = craft == null ? null : craft.getHandle();
			if (this.fukkit$getBukkit() instanceof Vehicle && ((EntityAccess) entity).getBukkit() instanceof org.bukkit.entity.LivingEntity) {
				VehicleExitEvent event = new VehicleExitEvent((Vehicle) this.fukkit$getBukkit(), (org.bukkit.entity.LivingEntity) ((EntityAccess) entity).getBukkit());
				Bukkit.getPluginManager().callEvent(event);
				CraftEntity craftn = (CraftEntity) ((EntityAccess<?>) entity).getBukkit().getVehicle();
				Entity n = craftn == null ? null : craftn.getHandle();
				if (event.isCancelled() || n != orig) {
					return false;
				}
			}


			this.passengerList.remove(entity);

			((EntityAccess) entity).setRidingCooldown(60);
		}
		return true;
	}

	@Redirect (method = "tickNetherPortal", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isNetherAllowed()Z"))
	private boolean fukkit_alwaysAllowNether(MinecraftServer server) {
		return true;
	}

	@Redirect (method = "tickNetherPortal", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;changeDimension(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/entity/Entity;"))
	private Entity fukkit_changeDim(Entity entity, DimensionType newDimension) {
		if (entity instanceof ServerPlayerEntity) {
			((ServerPlayerEntityAccess) entity).changeDimension(newDimension, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
		} else entity.changeDimension(newDimension);
		return entity;
	}

	@Inject (method = "setSwimming", at = @At ("HEAD"), cancellable = true)
	private void fukkit_swimEvent(boolean swimming, CallbackInfo ci) {
		if (this.isSwimming() != swimming && ((Entity) (Object) this) instanceof LivingEntity) {
			if (CraftEventFactory.callToggleSwimEvent((LivingEntity) (Object) this, swimming).isCancelled())
				ci.cancel();
		}
	}

	@Redirect (method = "setAir", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;set(Lnet/minecraft/entity/data/TrackedData;Ljava/lang/Object;)V"))
	private <T> void fukkit_airChangeEvent(DataTracker tracker, TrackedData<T> key, T object) {
		EntityAirChangeEvent event = new EntityAirChangeEvent((org.bukkit.entity.Entity) this.fukkit$getBukkit(), (Integer) object);
		if (this.valid) {
			event.getEntity().getServer().getPluginManager().callEvent(event);
		}

		if (!event.isCancelled()) {
			tracker.set(AIR, event.getAmount());
		}
	}

	@Inject (method = "onStruckByLightning", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V"))
	private void fukkit_entityCombustByEntityEvent(LightningEntity lightning, CallbackInfo ci) {
		EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(((EntityAccess<?>) lightning).getBukkit(), (org.bukkit.entity.Entity) this.fukkit$getBukkit(), 8);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled()) this.fukkit$setOnFireFor(event.getDuration(), false);
	}

	@Inject (method = "onStruckByLightning", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private void fukkit_hangingBreakByEntityEvent(LightningEntity lightning, CallbackInfo ci) {
		org.bukkit.entity.Entity thisBukkit = (org.bukkit.entity.Entity) this.fukkit$getBukkit();
		if (thisBukkit instanceof Hanging) {
			HangingBreakByEntityEvent event = new HangingBreakByEntityEvent((Hanging) thisBukkit, ((EntityAccess<?>) lightning).getBukkit());
			Bukkit.getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				ci.cancel();
				return;
			}
		}

		if (this.isFireImmune()) { // is this vanilla?
			ci.cancel();
			return;
		}

		CraftEventFactory.entityDamage = lightning;
		if (!this.damage(DamageSource.LIGHTNING_BOLT, 5.0f)) {
			CraftEventFactory.entityDamage = null;
			ci.cancel();
		}
	}

	@Redirect (method = "onStruckByLightning", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean fukkit_voidDamange(Entity entity, DamageSource source, float amount) {return false;}

	@Redirect (method = "onStruckByLightning", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V"))
	private void fukkit_redirectFireVoid(Entity entity, int seconds) {}


	public Entity fukkit$teleportTo(DimensionType type, BlockPos location) {
		if (!this.world.isClient && !this.removed) {
			this.world.getProfiler().push("changeDimension");
			MinecraftServer minecraftserver = this.getServer();
			DimensionType originalDimensionType = this.dimension;
			ServerWorld currentWorld = minecraftserver.getWorld(originalDimensionType);
			ServerWorld newWorld = minecraftserver.getWorld(type);
			if (newWorld == null) {
				return null;
			}

			this.world.getProfiler().push("reposition");
			Vec3d vec3d = this.getVelocity();
			float f = 0.0F;
			BlockPos blockposition = location;

			if (blockposition == null) {
				if (((DimensionTypeAccess) originalDimensionType).getType() == DimensionType.THE_END && type == DimensionType.OVERWORLD) {
					EntityPortalEvent event = CraftEventFactory.callEntityPortalEvent((Entity) (Object) this, newWorld, newWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, newWorld.getSpawnPos()), 0);
					if (event == null) {
						return null;
					}
					newWorld = ((CraftWorld) event.getTo().getWorld()).getHandle();
					blockposition = new BlockPos(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
				} else if (((DimensionTypeAccess) type).getType() == DimensionType.THE_END) {
					EntityPortalEvent event = CraftEventFactory.callEntityPortalEvent((Entity) (Object) this, newWorld, newWorld.getForcedSpawnPoint() != null ? newWorld.getForcedSpawnPoint() : newWorld.getSpawnPos(), 0);
					if (event == null) {
						return null;
					}
					newWorld = ((CraftWorld) event.getTo().getWorld()).getHandle();
					blockposition = new BlockPos(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
				} else {
					double d0 = this.getX();
					double d1 = this.getZ();
					double d2 = 8.0D;

					if (((DimensionTypeAccess) originalDimensionType).getType() == DimensionType.OVERWORLD && ((DimensionTypeAccess) type).getType() == DimensionType.THE_NETHER) {
						d0 /= 8.0D;
						d1 /= 8.0D;
					} else if (((DimensionTypeAccess) originalDimensionType).getType() == DimensionType.THE_NETHER && ((DimensionTypeAccess) type).getType() == DimensionType.OVERWORLD) {
						d0 *= 8.0D;
						d1 *= 8.0D;
					}

					double d3 = Math.min(-2.9999872E7D, newWorld.getWorldBorder().getBoundWest() + 16.0D);
					double d4 = Math.min(-2.9999872E7D, newWorld.getWorldBorder().getBoundNorth() + 16.0D);
					double d5 = Math.min(2.9999872E7D, newWorld.getWorldBorder().getBoundEast() - 16.0D);
					double d6 = Math.min(2.9999872E7D, newWorld.getWorldBorder().getBoundSouth() - 16.0D);

					d0 = MathHelper.clamp(d0, d3, d5);
					d1 = MathHelper.clamp(d1, d4, d6);
					Vec3d vec3d1 = this.getLastNetherPortalDirectionVector();

					blockposition = new BlockPos(d0, this.getY(), d1);
					EntityPortalEvent event = CraftEventFactory.callEntityPortalEvent((Entity) (Object) this, newWorld, blockposition, 128);
					if (event == null) {
						return null;
					}
					newWorld = ((CraftWorld) event.getTo().getWorld()).getHandle();
					blockposition = new BlockPos(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
					int searchRadius = event.getSearchRadius();
					BlockPattern.TeleportTarget shapedetector_shape = ((PortalForcerAccess) newWorld.getPortalForcer()).findPortal(blockposition, vec3d, this.getLastNetherPortalDirection(), vec3d1.x, vec3d1.y, ((Entity) (Object) this) instanceof PlayerEntity, searchRadius);

					if (shapedetector_shape == null) {
						return null;
					}

					blockposition = new BlockPos(shapedetector_shape.pos);
					vec3d = shapedetector_shape.velocity;
					f = (float) shapedetector_shape.yaw;
				}
			}

			this.dimension = type;
			this.detach();

			this.world.getProfiler().swap("reloading");
			Entity entity = this.getType().create((World) newWorld);

			if (entity != null) {
				entity.copyFrom((Entity) (Object) this);
				entity.refreshPositionAndAngles(blockposition, entity.yaw + f, entity.pitch);
				entity.setVelocity(vec3d);
				newWorld.onDimensionChanged(entity);

				((CraftEntity) this.fukkit$getBukkit()).setHandle(entity);
				((EntityAccess) entity).setBukkit((CraftEntity) this.fukkit$getBukkit());

				if (((Entity) (Object) this) instanceof MobEntity) {
					((MobEntity) (Object) this).detachLeash(true, false); // Unleash to prevent duping of leads.
				}

			}

			this.removed = true;
			this.world.getProfiler().pop();
			currentWorld.resetIdleTimeout();
			newWorld.resetIdleTimeout();
			this.world.getProfiler().pop();
			return entity;
		} else {
			return null;
		}
	}

	@ModifyVariable (method = "setBoundingBox", at = @At ("HEAD"))
	public Box fukkit_disableInvalids(Box newBox) {
		double minX = newBox.x1, minY = newBox.y1, minZ = newBox.z1, maxX = newBox.x2, maxY = newBox.y2, maxZ = newBox.z2;
		double len = newBox.x2 - newBox.x1;
		if (len < 0) maxX = minX;
		if (len > 64) maxX = minX + 64.0;

		len = newBox.y2 - newBox.y1;
		if (len < 0) maxY = minY;
		if (len > 64) maxY = minY + 64.0;

		len = newBox.z2 - newBox.z1;
		if (len < 0) maxZ = minZ;
		if (len > 64) maxZ = minZ + 64.0;
		return new Box(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public float fukkit$getYaw() {
		return this.yaw;
	}

	public boolean fukkit$isChunkLoaded() {
		return this.world.isChunkLoaded((int) Math.floor(this.x) >> 4, (int) Math.floor(this.z) >> 4);
	}

	public Object fukkit$getBukkit() {
		if (this.entity == null) {
			this.entity = CraftEntity.getEntity(((WorldAccess) this.world).getBukkitServer(), (Entity) (Object) this);
		}
		return this.entity;
	}

	public CraftEntity getBukkitEntity(){
		if (this.entity == null) {
			this.entity = CraftEntity.getEntity(((WorldAccess) this.world).getBukkitServer(), (Entity) (Object) this);
		}
		return this.entity;
	}

	public void fukkit$setBukkit(Object entity) {
		this.entity = (CraftEntity) entity;
	}

	public boolean fukkit$willPersist() {
		return this.persist;
	}

	public boolean fukkit$isValid() {
		return this.valid;
	}

	public ProjectileSource fukkit$getProjectileSource() {
		return this.projectileSource;
	}

	public boolean fukkit$shouldForceExplosionKnockback() {
		return this.forceExplosionKnockback;
	}

	public void fukkit$shouldPersist(boolean persist) {
		this.persist = persist;
	}

	public void fukkit$setValid(boolean valid) {
		this.valid = valid;
	}

	public void fukkit$setProjectileSource(ProjectileSource projectileSource) {
		this.projectileSource = projectileSource;
	}

	public void fukkit$setForceExplosionKnockback(boolean forceExplosionKnockback) {
		this.forceExplosionKnockback = forceExplosionKnockback;
	}

	public void fukkit$setRidingCooldown(int cooldown) {
		this.ridingCooldown = cooldown;
	}

	public int fukkit$getRidingCooldown() {
		return this.ridingCooldown;
	}
}