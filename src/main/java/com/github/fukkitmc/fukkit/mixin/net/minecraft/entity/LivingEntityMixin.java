package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.LivingEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.entity.EntityUtil;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.*;

@Mixin (LivingEntity.class)
@Implements (@Interface (iface = LivingEntityAccess.class, prefix = "fukkit$"))
public abstract class LivingEntityMixin extends EntityMixin {
	@Shadow
	@Final
	private static TrackedData<Float> HEALTH;

	@Shadow
	public abstract EntityAttributeInstance getAttributeInstance(EntityAttribute attribute);

	@Shadow
	public abstract float getHeadYaw();

	@Shadow
	protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

	@Shadow
	@Final
	private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

	@Shadow
	public abstract boolean canHaveStatusEffect(StatusEffectInstance effect);

	@Shadow
	protected abstract void onStatusEffectApplied(StatusEffectInstance effect);

	@Shadow
	protected abstract void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect);

	@Shadow
	public abstract float getHealth();

	@Shadow
	public abstract void setHealth(float health);

	@Shadow
	protected boolean dead;

	@Shadow
	protected abstract boolean blockedByShield(DamageSource source);

	@Shadow
	protected abstract void dropInventory();

	@Shadow
	protected abstract boolean canDropLootAndXp();

	@Shadow
	protected abstract boolean shouldAlwaysDropXp();

	@Shadow
	protected int playerHitTimer;

	@Shadow
	protected abstract int getCurrentExperience(PlayerEntity player);

	@Shadow
	protected PlayerEntity attackingPlayer;

	@Override
	@Shadow
	public native boolean damage(DamageSource source, float amount);

	@Shadow
	protected abstract float applyArmorToDamage(DamageSource source, float amount);

	@Shadow
	public abstract net.minecraft.item.ItemStack getEquippedStack(EquipmentSlot slot);

	@Shadow
	public abstract StatusEffectInstance getStatusEffect(StatusEffect effect);

	@Shadow
	public abstract boolean hasStatusEffect(StatusEffect effect);

	@Shadow
	protected abstract float applyEnchantmentsToDamage(DamageSource source, float amount);

	@Shadow
	public abstract float getAbsorptionAmount();

	@Shadow
	protected abstract void damageArmor(float amount);

	@Shadow
	protected abstract void damageShield(float amount);

	@Shadow
	protected abstract void takeShieldHit(LivingEntity attacker);

	@Shadow
	public abstract void setAbsorptionAmount(float amount);

	@Shadow
	public abstract DamageTracker getDamageTracker();

	@Shadow
	private AbstractEntityAttributeContainer attributes;

	@Shadow
	public abstract boolean isClimbing();

	@Shadow protected net.minecraft.item.ItemStack activeItemStack;

	@Shadow public abstract void setStackInHand(Hand hand, net.minecraft.item.ItemStack stack);

	@Shadow public abstract Hand getActiveHand();

	@Shadow public abstract boolean isSleeping();

	@Shadow public abstract boolean isAlive();

	@Shadow public abstract Optional<BlockPos> getSleepingPosition();

	@Shadow public abstract float getMaximumHealth();

	@Shadow protected abstract void dropLoot(DamageSource source, boolean causedByPlayer);

	@Shadow public abstract Collection<StatusEffectInstance> getStatusEffects();

	@Shadow public abstract void sleep(BlockPos pos);

	@Shadow public int deathTime;

	@Shadow public abstract void setStuckArrowCount(int stuckArrowCount);

	@Shadow protected abstract void markEffectsDirty();

	@Shadow public abstract void setAttacker(LivingEntity attacker);

	@Mutable
	@Shadow @Final private DamageTracker damageTracker;
	@Shadow private LivingEntity attacker;
	protected int expToDrop;
	protected int maxAirTicks = 300;
	protected boolean forceDrops;
	protected ArrayList<ItemStack> drops = new ArrayList<>();
	protected CraftAttributeMap craftAttributes;
	protected boolean collides = true;
	protected boolean canPickUpLoot;

	@Unique
	protected void fukkit_setDamageTracker(DamageTracker tracker) {
		this.damageTracker = tracker;
	}
	@Unique
	protected void fukkit_setAttacker(LivingEntity entity) {
		this.attacker = entity;
	}

	@Redirect (method = "<init>", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
	private void fukkit_avoidConstructorIssue(LivingEntity entity, float health) {
		this.dataTracker.set(HEALTH, (float) this.getAttributeInstance(EntityAttributes.MAX_HEALTH).getValue());
	}

	public float fukkit$getYaw() {
		return this.getHeadYaw();
	}

	@Redirect (method = "fall", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
	private int fukkit_visibilityAPI(ServerWorld world, ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
		if ((Object) this instanceof ServerPlayerEntity) {
			((ServerWorldAccess) world).spawnParticles((ServerPlayerEntity) (Object) this, new BlockStateParticleEffect(ParticleTypes.BLOCK, landedState), this.getX(), this.getY(), this.getZ(), count, 0, 0, 0, .15, false);
		} else {
			world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, landedState), this.getX(), this.getY(), this.getZ(), count, 0, 0, 0, .15);
		}
		return 0;
	}

	@Inject (method = "updatePostDeath", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;remove()V"))
	private void fukkit_ticks(CallbackInfo ci) {
		if (this.removed) ci.cancel();
	}

	@Inject (method = "readCustomDataFromTag", at = @At (value = "CONSTANT", args = "stringValue=Health"))
	private void fukkit_maxHealth(CompoundTag ctag, CallbackInfo ci) {
		if (ctag.contains("Bukkit.MaxHealth")) {
			Tag tag = ctag.get("Bukkit.MaxHealth");
			assert tag != null : "ohno";
			if (tag.getType() == 5) {
				this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(((FloatTag) tag).getDouble());
			} else if (tag.getType() == 3) {
				this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(((IntTag) tag).getDouble());
			}
		}
	}

	private boolean isTickingEffects = false;
	private List<EntityUtil.ProcessableEffect> effectsToProcess = Lists.newArrayList();

	@Inject (method = "tickStatusEffects", at = @At ("HEAD"))
	private void fukkit_tick(CallbackInfo ci) {
		this.isTickingEffects = true;
	}

	private Iterator<?> iterator;

	@Redirect (method = "tickStatusEffects", at = @At (value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
	private void fukkit_potionEvent(Iterator<?> iterator) {
		this.iterator = iterator;
	}

	@Redirect (method = "tickStatusEffects", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"))
	private void fukkit_potionEvent(LivingEntity entity, StatusEffectInstance effect) {
		Iterator<?> iterator = this.iterator;
		this.iterator = null;
		EntityPotionEffectEvent event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, effect, null, EntityPotionEffectEvent.Cause.EXPIRATION);
		if (!event.isCancelled()) {
			iterator.remove();
			this.onStatusEffectRemoved(effect);
		}
	}

	@Inject (method = "tickStatusEffects", at = @At (value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;effectsChanged:Z"))
	private void fukkit_process(CallbackInfo ci) {
		this.isTickingEffects = false;
		for (EntityUtil.ProcessableEffect process : this.effectsToProcess) {
			if (process.getEffect() != null) this.fukkit$addEffect(process.getEffect(), process.getCause());
			else this.fukkit$removeEffect(process.getType(), process.getCause());
		}
	}

	/**
	 * todo replace with injects
	 *
	 * @reason because I'm lazy
	 * @author HalfOf2
	 */
	@Overwrite
	public boolean clearStatusEffects() {
		return this.fukkit$removeAllEffects(EntityPotionEffectEvent.Cause.UNKNOWN);
	}

	public boolean fukkit$removeAllEffects(EntityPotionEffectEvent.Cause cause) {
		// CraftBukkit end
		if (this.world.isClient) {
			return false;
		} else {
			Iterator<StatusEffectInstance> iterator = this.activeStatusEffects.values().iterator();
			boolean flag;
			for (flag = false; iterator.hasNext(); flag = true) {
				// CraftBukkit start
				StatusEffectInstance effect = iterator.next();
				EntityPotionEffectEvent event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, effect, null, cause, EntityPotionEffectEvent.Action.CLEARED);
				if (event.isCancelled()) {
					continue;
				}
				this.onStatusEffectRemoved(effect);
				// CraftBukkit end
				iterator.remove();
			}

			return flag;
		}
	}

	/**
	 * todo replace with injects
	 *
	 * @reason functionality overwritten
	 * @author HalfOf2
	 */
	@Overwrite
	public boolean addStatusEffect(StatusEffectInstance effect) {
		return this.fukkit$addEffect(effect, EntityPotionEffectEvent.Cause.UNKNOWN);
	}

	public boolean fukkit$addEffect(StatusEffectInstance effect, EntityPotionEffectEvent.Cause cause) {
		if (this.isTickingEffects) {
			this.effectsToProcess.add(new EntityUtil.ProcessableEffect(effect, cause));
			return true;
		}
		if (!this.canHaveStatusEffect(effect)) {
			return false;
		} else {
			StatusEffectInstance newType = this.activeStatusEffects.get(effect.getEffectType());
			boolean override = false;
			if (newType != null) {
				override = new StatusEffectInstance(newType).upgrade(effect);
			}

			EntityPotionEffectEvent event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, newType, effect, cause, override);
			if (event.isCancelled()) {
				return false;
			}

			if (newType == null) {
				this.activeStatusEffects.put(effect.getEffectType(), effect);
				this.onStatusEffectApplied(effect);
				return true;
			} else if (event.isOverride()) {
				newType.upgrade(effect);
				this.onStatusEffectUpgraded(newType, true);
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * todo replace with injects
	 *
	 * @reason functionality replaced
	 * @author HalfOf2
	 */
	@SuppressWarnings ("OverwriteModifiers")
	@Overwrite
	public StatusEffectInstance removeStatusEffectInternal(StatusEffect effect) {
		return this.fukkit$removeStatusEffectInternal(effect, EntityPotionEffectEvent.Cause.UNKNOWN);
	}

	public StatusEffectInstance fukkit$removeStatusEffectInternal(StatusEffect effect, EntityPotionEffectEvent.Cause cause) {
		if (this.isTickingEffects) {
			this.effectsToProcess.add(new EntityUtil.ProcessableEffect(effect, cause));
			return null;
		}
		StatusEffectInstance active = this.activeStatusEffects.get(effect);
		if (active == null) {
			return null;
		}
		EntityPotionEffectEvent event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, active, null, cause);
		if (event.isCancelled()) {
			return null;
		}
		return this.activeStatusEffects.remove(effect);
	}

	/**
	 * todo replace with injects
	 *
	 * @author HalfOf2
	 * @reason method sig change
	 */
	@Overwrite
	public boolean removeStatusEffect(StatusEffect effect) {
		return this.fukkit$removeEffect(effect, EntityPotionEffectEvent.Cause.UNKNOWN);
	}

	public boolean fukkit$removeEffect(StatusEffect effect, EntityPotionEffectEvent.Cause cause) {
		StatusEffectInstance removed = this.fukkit$removeStatusEffectInternal(effect, cause);
		if (removed != null) {
			this.onStatusEffectRemoved(removed);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * todo replace with injects
	 *
	 * @reason md5pls
	 * @author HalfOf2
	 */
	@Overwrite
	public void heal(float health) {
		this.fukkit$heal(health, EntityRegainHealthEvent.RegainReason.CUSTOM);
	}

	public void fukkit$heal(float f, EntityRegainHealthEvent.RegainReason regainReason) {
		float f1 = this.getHealth();
		if (f1 > 0.0F) {
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(((LivingEntityAccess<?>) this).getBukkit(), f, regainReason);
			if (((LivingEntityAccess) this).isValid()) {
				((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);
			}

			if (!event.isCancelled()) {
				this.setHealth((float) (this.getHealth() + event.getAmount()));
			}
		}
	}

	@Inject (method = "getHealth", at = @At ("HEAD"))
	private void fukkit_unscaled(CallbackInfoReturnable<Float> cir) {
		if ((Object) this instanceof ServerPlayerEntity) {
			cir.setReturnValue((float) ((ServerPlayerEntityAccess) this).getBukkit().getHealth());
		}
	}

	@Inject (method = "setHealth", at = @At ("HEAD"))
	private void fukkit_scaledHealth(float health, CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayerEntity) {
			CraftPlayer player = ((ServerPlayerEntityAccess) this).getBukkit();
			if (health < 0) {
				player.setRealHealth(0);
			} else if (health > player.getMaxHealth()) {
				player.setRealHealth(player.getMaxHealth());
			} else {
				player.setRealHealth(health);
			}
			player.updateScaledHealth(false);
			ci.cancel();
		}
	}

	// not vanilla
	@Redirect (method = "damage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getHealth()F", ordinal = 0))
	private float fukkit_nokillRemoved(LivingEntity entity) {
		return this.removed || this.dead ? -1 : this.getHealth();
	}

	@Redirect (method = "damage", at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
	private boolean fukkit_empty(net.minecraft.item.ItemStack stack) {
		return true;
	}

	@ModifyVariable (method = "damage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;blockedByShield(Lnet/minecraft/entity/damage/DamageSource;)Z"), index = 4)
	private boolean fukkit_belowCopied(boolean current, DamageSource source, float amount) {
		return amount > 0 // this is safe:tm:
		       && this.blockedByShield(source);
	}

	@Redirect (method = "damage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;blockedByShield(Lnet/minecraft/entity/damage/DamageSource;)Z"))
	private boolean fukkit_moved(LivingEntity entity, DamageSource source) {
		return false;
	}

	@Inject (method = "damage", at = @At (value = "RETURN", ordinal = 0), slice = @Slice (from = @At (value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastDamageTaken:F", ordinal = 0)))
	private void fukkit_vanillaConsistancy(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		((EntityAccess) this).setForceExplosionKnockback(true);
	}

	// 2 birds 1 stone
	@Redirect (method = "damage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
	private void fukkit_handle(LivingEntity entity, DamageSource source, float amount) {}

	@Inject (method = "damage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", shift = At.Shift.AFTER), cancellable = true)
	private void fukkit_return(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!this.fukkit$damage0(source, amount)) cir.setReturnValue(false);
	}

	@Inject (method = "damage", at = @At (value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastDamageTaken:F", ordinal = 1, opcode = Opcodes.PUTFIELD))
	private void fukkit_damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!this.fukkit$damage0(source, amount)) cir.setReturnValue(false);
	}

	@Inject (method = "damage", at = @At (value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;knockbackVelocity:F", opcode = Opcodes.PUTFIELD, ordinal = 0))
	private void fukkit_love(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		Object _this = this;
		if (_this instanceof AnimalEntity) {
			((AnimalEntity) _this).resetLoveTicks();
			if (_this instanceof TameableEntity) ((TameableEntity) _this).getSitGoal().setEnabledWithOwner(false);
		}
	}

	private net.minecraft.item.ItemStack stack = net.minecraft.item.ItemStack.EMPTY;
	private boolean cancelled;

	@Redirect (method = "tryUseTotem", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
	private net.minecraft.item.ItemStack fukkit_useTotem(LivingEntity entity, Hand hand) {
		return this.stack = entity.getStackInHand(hand);
	}

	@Redirect (method = "tryUseTotem", at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private void fukkit_tryTotem(net.minecraft.item.ItemStack stack, int amount) {}

	@ModifyVariable (method = "tryUseTotem", at = @At (value = "LOAD"), slice = @Slice (from = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"), to = @At (value = "JUMP", opcode = Opcodes.IFNULL, ordinal = 0)), index = 4, ordinal = 0)
	private net.minecraft.item.ItemStack fukkit_temp(net.minecraft.item.ItemStack stack) {
		EntityResurrectEvent event = new EntityResurrectEvent((org.bukkit.entity.LivingEntity) this.fukkit$getBukkit());
		event.setCancelled(stack == null);
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);
		this.cancelled = event.isCancelled();
		if (this.cancelled && !this.stack.isEmpty()) this.stack.decrement(1);

		this.stack = stack;
		return this.cancelled ? null : stack;
	}

	@SuppressWarnings ("ConstantConditions")
	@Redirect (method = "tryUseTotem", at = @At (value = "OPCODE", opcode = Opcodes.INSTANCEOF))
	private boolean fukkit_redir(Object object, Class type) {
		boolean ret = this.stack != null && (Object) this instanceof ServerPlayerEntity;
		this.stack = null;
		return ret;
	}

	@Redirect (method = "tryUseTotem", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z"))
	private boolean fukkit_removeAll(LivingEntity entity) {
		return this.fukkit$removeAllEffects(EntityPotionEffectEvent.Cause.TOTEM);
	}

	// 2 birds with 1 stone
	@Redirect (method = "tryUseTotem", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
	private boolean fukkit_add(LivingEntity entity, StatusEffectInstance effect) {
		return this.fukkit$addEffect(effect, EntityPotionEffectEvent.Cause.TOTEM);
	}

	@Inject (method = "tryUseTotem", at = @At ("TAIL"))
	private void fukkit_return(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(!this.cancelled);
		this.stack = null;
	}

	@Inject (method = "tryUseTotem", at = @At ("HEAD"))
	private void fukkit_reset(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		this.stack = net.minecraft.item.ItemStack.EMPTY;
	}

	@Inject (method = "drop", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;canDropLootAndXp()Z"))
	private void fukkit_dropInv(DamageSource source, CallbackInfo ci) {
		this.dropInventory();
	}

	@Inject (method = "drop", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropEquipment(Lnet/minecraft/entity/damage/DamageSource;IZ)V", shift = At.Shift.AFTER))
	private void fukkit_deathEvent(DamageSource source, CallbackInfo ci) {
		CraftEventFactory.callEntityDeathEvent((LivingEntity) (Object) this, this.drops);
		this.drops = new ArrayList<>();
	}

	@Redirect (method = "drop", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V"))
	private void fukkit_movedUp(LivingEntity entity) {
		// else block logic handled here
		if (!this.canDropLootAndXp() || !this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
			CraftEventFactory.callEntityDeathEvent((LivingEntity) (Object) this);
		}
	}

	public int fukkit$getExpReward() {
		if (!this.world.isClient && (this.shouldAlwaysDropXp() || this.playerHitTimer > 0 && this.canDropLootAndXp() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))) {
			return this.getCurrentExperience(this.attackingPlayer);
		} else {
			return 0;
		}
	}

	/**
	 * @reason many redirects needed, not in the mood to do them
	 * @author HalfOf2
	 */
	@Overwrite
	public void dropXp() {
		int toDrop = this.expToDrop;
		while (toDrop > 0) {
			int rounded = ExperienceOrbEntity.roundToOrbSize(toDrop);
			toDrop -= rounded;
			this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY(), this.getZ(), rounded));
		}
		this.expToDrop = 0;
	}

	@Inject (method = "handleFallDamage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_damage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Boolean> cir, boolean bl, int computed) {
		if (!this.damage(DamageSource.FALL, computed)) cir.setReturnValue(true);
	}

	@Redirect (method = "handleFallDamage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean fukkit_movedUp(LivingEntity entity, DamageSource source, float amount) {return false;}

	@Redirect (method = "applyArmorToDamage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageArmor(F)V"))
	private void fukkit_handled_damage0(LivingEntity entity, float amount) {}

	@Redirect (method = "applyEnchantmentsToDamage", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
	private boolean fukkit_handled_damage0(LivingEntity entity, StatusEffect effect) {
		return false;
	}

	/**
	 * todo replace with injects when project complete
	 * @author HalfOf2
	 * @reason to remind me that I have called the wrong method
	 */
	@Overwrite
	public void applyDamage(DamageSource source, float damage) {
		throw new IllegalStateException("Half is an idiot and forgot to replace this with duplicated injects");
	}

	// concern
	public boolean fukkit$damage0(final DamageSource source, float f) { // void -> boolean, add final
		if (!this.isInvulnerableTo(source)) {
			final boolean human = (Object) this instanceof PlayerEntity;
			float originalDamage = f;
			Function<Double, Double> hardHat = f12 -> {
				if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
					return -(f12 - (f12 * 0.75F));

				}
				return -0.0;
			};
			float hardHatModifier = hardHat.apply((double) f).floatValue();
			f += hardHatModifier;

			Function<Double, Double> blocking = f13 -> -((this.blockedByShield(source)) ? f13 : 0.0);
			float blockingModifier = blocking.apply((double) f).floatValue();
			f += blockingModifier;

			Function<Double, Double> armor = f14 -> -(f14 - this.applyArmorToDamage(source, f14.floatValue()));
			float armorModifier = armor.apply((double) f).floatValue();
			f += armorModifier;

			Function<Double, Double> resistance = f15 -> {
				if (!source.isUnblockable() && this.hasStatusEffect(StatusEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
					int i = (this.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
					int j = 25 - i;
					float f1 = f15.floatValue() * (float) j;
					return -(f15 - (f1 / 25.0F));
				}
				return -0.0;
			};
			float resistanceModifier = resistance.apply((double) f).floatValue();
			f += resistanceModifier;

			Function<Double, Double> magic = f16 -> -(f16 - this.applyEnchantmentsToDamage(source, f16.floatValue()));
			float magicModifier = magic.apply((double) f).floatValue();
			f += magicModifier;

			Function<Double, Double> absorption = f17 -> -(Math.max(f17 - Math.max(f17 - this.getAbsorptionAmount(), 0.0F), 0.0F));
			float absorptionModifier = absorption.apply((double) f).floatValue();

			EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent((LivingEntity) (Object) this, source, originalDamage, hardHatModifier, blockingModifier, armorModifier, resistanceModifier, magicModifier, absorptionModifier, hardHat, blocking, armor, resistance, magic, absorption);
			if (event.isCancelled()) {
				return false;
			}

			f = (float) event.getFinalDamage();

			// Resistance
			if (event.getDamage(EntityDamageEvent.DamageModifier.RESISTANCE) < 0) {
				float f3 = (float) -event.getDamage(EntityDamageEvent.DamageModifier.RESISTANCE);
				if (f3 > 0.0F && f3 < 3.4028235E37F) {
					if ((Object) this instanceof ServerPlayerEntity) {
						((ServerPlayerEntity) (Object) this).increaseStat(Stats.DAMAGE_RESISTED, Math.round(f3 * 10.0F));
					} else if (source.getAttacker() instanceof ServerPlayerEntity) {
						((ServerPlayerEntity) source.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f3 * 10.0F));
					}
				}
			}

			// Apply damage to helmet
			if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && this.getEquippedStack(EquipmentSlot.HEAD) != null) {
				this.getEquippedStack(EquipmentSlot.HEAD).damage((int) (event.getDamage() * 4.0F + this.random.nextFloat() * event.getDamage() * 2.0F), (LivingEntity) (Object) this, (living) -> {
					living.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
				});
			}

			// Apply damage to armor
			if (!source.bypassesArmor()) {
				float armorDamage = (float) (event.getDamage() + event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) + event.getDamage(EntityDamageEvent.DamageModifier.HARD_HAT));
				this.damageArmor(armorDamage);
			}

			// Apply blocking code // PAIL: steal from above
			if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0) {
				this.world.sendEntityStatus((LivingEntity) (Object) this, (byte) 29); // SPIGOT-4635 - shield damage sound
				this.damageShield((float) -event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING));
				Entity entity = source.getSource();

				if (entity instanceof net.minecraft.entity.LivingEntity) {
					this.takeShieldHit((net.minecraft.entity.LivingEntity) entity);
				}
			}

			absorptionModifier = (float) -event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
			this.setAbsorptionAmount(Math.max(this.getAbsorptionAmount() - absorptionModifier, 0.0F));
			float f2 = absorptionModifier;

			if (f2 > 0.0F && f2 < 3.4028235E37F && (Object) this instanceof PlayerEntity) {
				((PlayerEntity) (Object) this).increaseStat(Stats.DAMAGE_ABSORBED, Math.round(f2 * 10.0F));
			}
			if (f2 > 0.0F && f2 < 3.4028235E37F && source.getAttacker() instanceof ServerPlayerEntity) {
				((ServerPlayerEntity) source.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f2 * 10.0F));
			}

			if (f > 0 || !human) {
				if (human) {
					// PAIL: Be sure to drag all this code from the EntityHuman subclass each update.
					((PlayerEntity) (Object) this).addExhaustion(source.getExhaustion());
					if (f < 3.4028235E37F) {
						((PlayerEntity) (Object) this).increaseStat(Stats.DAMAGE_TAKEN, Math.round(f * 10.0F));
					}
				}
				// CraftBukkit end
				float f3 = this.getHealth();

				this.setHealth(f3 - f);
				this.getDamageTracker().onDamage(source, f3, f);
				// CraftBukkit start
				if (!human) {
					this.setAbsorptionAmount(this.getAbsorptionAmount() - f);
				}

				return true;
			} else {
				// Duplicate triggers if blocking
				if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0) {
					if ((Object) this instanceof ServerPlayerEntity) {
						Criterions.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity) (Object) this, source, f, originalDamage, true);
						f2 = (float) -event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING);
						if (f2 > 0.0F && f2 < 3.4028235E37F) {
							((ServerPlayerEntity) (Object) this).increaseStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(originalDamage * 10.0F));
						}
					}

					if (source.getAttacker() instanceof ServerPlayerEntity) {
						Criterions.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity) source.getAttacker(), (Entity) (Object) this, source, f, originalDamage, true);
					}

					return false;
				} else {
					return originalDamage > 0;
				}
				// CraftBukkit end
			}
		}
		return false; // CraftBukkit
	}

	@Inject (method = "getAttributes", at = @At (value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;attributes:Lnet/minecraft/entity/attribute/AbstractEntityAttributeContainer;", shift = At.Shift.AFTER))
	private void fukkit_craftAttributes(CallbackInfoReturnable<AbstractEntityAttributeContainer> cir) {
		this.craftAttributes = new CraftAttributeMap(this.attributes);
	}

	@Redirect (method = "travel", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setFlag(IZ)V"))
	private void fukkit_toggleGlide(LivingEntity entity, int index, boolean value) {
		if (this.getFlag(7 /* yay magic numbers */) && !CraftEventFactory.callToggleGlideEvent((LivingEntity) (Object) this, false).isCancelled())
			this.setFlag(7, false);
	}

	@Redirect (method = "initAi", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setFlag(IZ)V"))
	private void fukkit_toggleGlide0(LivingEntity entity, int index, boolean value) {
		if (value ^ this.getFlag(7) && !CraftEventFactory.callToggleGlideEvent((LivingEntity) (Object) this, value).isCancelled())
			this.setFlag(7, value);
	}

	@Inject (method = "collides", at = @At ("RETURN"), cancellable = true)
	private void fukkit_collides(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(cir.getReturnValue() && this.collides);
	}

	@Redirect (method = "isPushable", at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isClimbing()Z"))
	private boolean fukkit_collides0(LivingEntity entity) {
		return !this.isClimbing() && this.collides;
	}

	@Inject (method = "consumeItem", at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
	private void fukkit_consumeEvent(CallbackInfo ci) {
		net.minecraft.item.ItemStack stacc;
		if((Object) this instanceof ServerPlayerEntity) {
			ItemStack craft = CraftItemStack.asBukkitCopy(this.activeItemStack);
			PlayerItemConsumeEvent event = new PlayerItemConsumeEvent((Player) this.fukkit$getBukkit(), craft);
			((WorldAccess)this.world).getBukkitServer().getPluginManager().callEvent(event);
			if(event.isCancelled()) {
				// update client
				CraftPlayer access = (CraftPlayer) this.fukkit$getBukkit();
				access.updateInventory();
				access.updateScaledHealth();
				ci.cancel();
				return;
			}
			stacc = craft.equals(event.getItem()) ? this.activeItemStack.finishUsing(this.world, (LivingEntity) (Object)this) : CraftItemStack.asNMSCopy(event.getItem()).finishUsing(this.world, (LivingEntity) (Object) this);
		} else {
			stacc = this.activeItemStack.finishUsing(this.world, (LivingEntity) (Object)this);
		}
		this.setStackInHand(this.getActiveHand(), stacc);
	}

	@Redirect(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"))
	private void fukkit_set(LivingEntity entity, Hand hand, net.minecraft.item.ItemStack stack) {}
	@Redirect (method = "consumeItem", at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
	private net.minecraft.item.ItemStack voidCall(net.minecraft.item.ItemStack stack, World world, LivingEntity user) {
		return stack;
	}

	private boolean pass = false;
	@Inject(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;doesNotCollide(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_teleportEvent(double x, double y, double z, boolean particleEffects, CallbackInfoReturnable<Boolean> cir, double tx, double ty, double tz, double y2, boolean bl, BlockPos pos, World world, boolean bl2) {
		CraftWorld craft = ((WorldAccess)this.world).getBukkit();
		EntityTeleportEvent event = new EntityPortalEvent((org.bukkit.entity.Entity) this.fukkit$getBukkit(), new Location(craft, tx, ty, tz), new Location(craft, x, y, z));
		((WorldAccess)this.world).getBukkitServer().getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			Location to = event.getTo();
			this.requestTeleport(to.getX(), to.getY(), to.getZ());
			if(world.doesNotCollide((Entity) (Object) this) && !world.containsFluid(this.getBoundingBox()))
				this.pass = true;
		}
	}

	@Redirect(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;requestTeleport(DDD)V"))
	private void fukkit_void(LivingEntity entity, double destX, double destY, double destZ) {}
	@Redirect(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;doesNotCollide(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_void(World world, Entity entity) {
		return this.pass;
	}
	@Redirect(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;containsFluid(Lnet/minecraft/util/math/Box;)Z"))
	private boolean fukkit_true(World world, Box box) {
		return false;
	}

	@Redirect(method = "applyFoodEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
	private boolean fukkit_eat(LivingEntity entity, StatusEffectInstance effect) {
		return this.fukkit$addEffect(effect, EntityPotionEffectEvent.Cause.FOOD);
	}

	public int fukkit$getExpToDrop() {
		return this.expToDrop;
	}

	public int fukkit$getMaxAirTicks() {
		return this.maxAirTicks;
	}

	public boolean fukkit$shouldForceDrops() {
		return this.forceDrops;
	}

	public ArrayList<ItemStack> fukkit$getDrops() {
		return this.drops;
	}

	public CraftAttributeMap fukkit$getCraftAttributes() {
		return this.craftAttributes;
	}

	public boolean fukkit$collides() {
		return this.collides;
	}

	public boolean fukkit$canPickUpLoot() {
		return this.canPickUpLoot;
	}

	public void fukkit$setExpToDrop(int exp) {
		this.expToDrop = exp;
	}

	public void fukkit$setMaxAirTicks(int ticks) {
		this.maxAirTicks = ticks;
	}

	public void fukkit$setForceDrops(boolean forceDrops) {
		this.forceDrops = forceDrops;
	}

	public void fukkit$setCraftAttributes(CraftAttributeMap map) {
		this.craftAttributes = map;
	}

	public void fukkit$setCollides(boolean collides) {
		this.collides = collides;
	}

	public void fukkit$setCanPickUpLoot(boolean canPickUpLoot) {
		this.canPickUpLoot = canPickUpLoot;
	}

}