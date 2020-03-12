package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.explosion;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.explosion.ExplosionAccess;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Implements (@Interface (iface = ExplosionAccess.class, prefix = "fukkit$"))
@Mixin (Explosion.class)
public class ExplosionMixin {
	// unsafe: probably safe, like 90%
	private static final ThreadLocal<Float> YIELD = new ThreadLocal<>();
	public boolean cancelled;
	@Mutable
	@Shadow
	@Final
	private float power;
	@Shadow
	@Final
	private World world;
	@Shadow
	@Final
	private Entity entity;
	@Shadow
	@Final
	private double x;
	@Shadow
	@Final
	private double y;
	@Shadow
	@Final
	private double z;
	@Shadow
	@Final
	private List<BlockPos> affectedBlocks;
	@Shadow
	@Final
	private Explosion.DestructionType blockDestructionType;
	private boolean cont = false;

	@Inject (method = "method_24023", at = @At ("HEAD"))
	private static void fukkit_isEmpty(ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList, ItemStack itemStack, BlockPos blockPos, CallbackInfo ci) {
		if (itemStack.isEmpty()) { ci.cancel(); }
	}

	@Inject (
	method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;" +
	         "DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V",
	at = @At ("RETURN"))
	private void fukkit_clampBad(World world, Entity entity, double x, double y, double z, float power,
	                             boolean createFire, Explosion.DestructionType blockDestructionType, CallbackInfo ci) {
		this.power = Math.max(power, 0.0f);
	}

	@Inject (method = "collectBlocksAndDamageEntities", at = @At ("HEAD"), cancellable = true)
	private void fukkit_ignoreMicroExplosions(CallbackInfo ci) {
		if (this.power < .1f) // not vanilla?
		{ ci.cancel(); }
	}

	@Redirect (method = "collectBlocksAndDamageEntities",
	           at = @At (value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
	private boolean fukkit_noWrap(Set set, Object e) { // not vanilla?
		BlockPos pos = (BlockPos) e;
		if (pos.getY() < 256 && pos.getY() >= 0) {
			set.add(e);
		}
		return false;
	}

	@Redirect (method = "collectBlocksAndDamageEntities", at = @At (value = "INVOKE",
	                                                                target = "Lnet/minecraft/entity/Entity;damage" +
	                                                                         "(Lnet/minecraft/entity/damage" +
	                                                                         "/DamageSource;F)Z"))
	private boolean fukkit_damage(Entity entity, DamageSource source, float amount) {
		CraftEventFactory.entityDamage = entity;
		((EntityAccess) entity).setForceExplosionKnockback(false);
		boolean wasDamaged = entity.damage(source, amount);
		CraftEventFactory.entityDamage = null;
		this.cont = !wasDamaged && !(entity instanceof TntEntity || entity instanceof FallingBlockEntity) && !((EntityAccess) entity)
		                                                                                                      .shouldForceExplosionKnockback();
		return false;
	}

	// hack to implement `continue;`, we redirect all the ifs and methods in the rest of the loop.
	@Redirect (method = "collectBlocksAndDamageEntities",
	           at = @At (value = "CONSTANT", args = "classValue=net.minecraft.entity.LivingEntity"))
	private boolean fukkit_continue(Object object, Class _class) {
		if (this.cont) { return false; } else { return object instanceof LivingEntity; }
	}

	@Redirect (method = "collectBlocksAndDamageEntities", at = @At (value = "INVOKE",
	                                                                target = "Lnet/minecraft/entity/Entity;setVelocity" +
	                                                                         "(Lnet/minecraft/util/math/Vec3d;)V"))
	private void fukkit_continue(Entity entity, Vec3d velocity) {
		if (!this.cont) {
			entity.setVelocity(velocity);
		}
	}

	@Redirect (method = "collectBlocksAndDamageEntities",
	           at = @At (value = "CONSTANT", args = "classValue=net.minecraft.entity.player.PlayerEntity"))
	private boolean fukkit_continue0(Object object, Class _class) {
		if (this.cont) { return false; } else { return object instanceof PlayerEntity; }
	}

	@Inject (method = "affectWorld",
	         at = @At (value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0))
	private void fukkit_explodeEvent(boolean bl, CallbackInfo ci) {
		CraftWorld craftWorld = ((WorldAccess) this.world).getBukkit();
		org.bukkit.entity.Entity explosive = this.entity == null ? null : ((EntityAccess<?>) this.entity).getBukkit();
		Location location = new Location(craftWorld, this.x, this.y, this.z);
		List<Block> blocks = new ArrayList<>();
		for (int i = this.affectedBlocks.size() - 1; i >= 0; i--) {
			BlockPos pos = this.affectedBlocks.get(i);
			Block block = craftWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
			if (!block.getType().isAir()) {
				blocks.add(block);
			}
		}

		boolean cancelled;
		List<Block> bukkit;
		float yield;

		if (explosive != null) {
			EntityExplodeEvent event = new EntityExplodeEvent(explosive, location, blocks,
			this.blockDestructionType == Explosion.DestructionType.DESTROY ? 1.0f / this.power : 1.0f);
			((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);
			cancelled = event.isCancelled();
			bukkit = event.blockList();
			yield = event.getYield();
		} else {
			BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blocks,
			this.blockDestructionType == Explosion.DestructionType.DESTROY ? 1.0f / this.power : 1.0f);
			((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);
			cancelled = event.isCancelled();
			bukkit = event.blockList();
			yield = event.getYield();
		}

		this.affectedBlocks.clear();

		for (Block block : bukkit) {
			BlockPos coords = new BlockPos(block.getX(), block.getY(), block.getZ());
			this.affectedBlocks.add(coords);
		}

		if (cancelled) {
			this.cancelled = true;
			ci.cancel();
		}

		YIELD.set(yield);
	}

	@Inject (method = "affectWorld", at = @At (value = "INVOKE",
	                                           target = "Lnet/minecraft/block/BlockState;getDroppedStacks" +
	                                                    "(Lnet/minecraft/loot/context/LootContext$Builder;)" +
	                                                    "Ljava/util/List;"), locals = LocalCapture.PRINT)
	private void fukkit_addYield(boolean bl, CallbackInfo ci) {
		float yield = YIELD.get();
		if (yield < 1.0f) {
		}
	}

	public boolean fukkit$wasCancelled() {
		return this.cancelled;
	}

	public void fukkit$setWasCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
