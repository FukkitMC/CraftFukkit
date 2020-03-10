package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.decoration;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Hanging;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (AbstractDecorationEntity.class)
public abstract class AbstractDecorationEntityMixin extends Entity {
	public AbstractDecorationEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject (method = "tick", at = @At (value = "INVOKE",
	                                    target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;remove()" +
	                                             "V"),
	         cancellable = true)
	public void breakEvent(CallbackInfo ci) {
		Material material = this.world.getBlockState(new BlockPos(this)).getMaterial();
		HangingBreakEvent.RemoveCause cause;

		if (!material.equals(Material.AIR)) {
			// TODO: This feels insufficient to catch 100% of suffocation cases
			cause = HangingBreakEvent.RemoveCause.OBSTRUCTION;
		} else {
			cause = HangingBreakEvent.RemoveCause.PHYSICS;
		}

		HangingBreakEvent event = new HangingBreakEvent((Hanging) ((CraftHandled<CraftEntity>) this)
		                                                          .getBukkit(), cause);
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);

		if (this.removed || event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject (method = "damage", at = @At (value = "INVOKE",
	                                      target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;remove()" +
	                                               "V"),
	         cancellable = true)
	public void damageEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		// CraftBukkit start - fire break events
		Entity damager = (source instanceof ProjectileDamageSource) ? source.getSource() : source.getAttacker();
		HangingBreakEvent event;
		if (damager != null) {
			event = new HangingBreakByEntityEvent((Hanging) ((CraftHandled<CraftEntity>) this)
			                                                .getBukkit(), ((CraftHandled<CraftEntity>) damager)
			                                                              .getBukkit(),
			source.isExplosive() ? HangingBreakEvent.RemoveCause.EXPLOSION : HangingBreakEvent.RemoveCause.ENTITY);
		} else {
			event = new HangingBreakEvent((Hanging) this,
			source.isExplosive() ? HangingBreakEvent.RemoveCause.EXPLOSION : HangingBreakEvent.RemoveCause.DEFAULT);
		}

		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);

		if (this.removed || event.isCancelled()) {
			cir.setReturnValue(true);
		}
	}

	@Inject (method = "move", at = @At (value = "INVOKE",
	                                    target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;remove()" +
	                                             "V"),
	         cancellable = true)
	public void moveEvent(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (this.removed) {
			return; // CraftBukkit
		}

		HangingBreakEvent event = new HangingBreakEvent((Hanging) ((CraftHandled<CraftEntity>) this)
		                                                          .getBukkit(), HangingBreakEvent.RemoveCause.PHYSICS);
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);

		if (this.removed || event.isCancelled()) {
			ci.cancel();
		}
	}
}
