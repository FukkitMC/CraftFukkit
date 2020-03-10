package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.player;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.HungerManagerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Objects;

@Implements (@Interface (iface = HungerManagerAccess.class, prefix = "fukkit$"))
@Mixin (HungerManager.class)
public abstract class HungerManagerMixin {
	@Shadow
	private int foodLevel;
	@Shadow private float foodSaturationLevel;
	private PlayerEntity player;

	public void fukkit$setPlayer(PlayerEntity entity) {
		this.player = Objects.requireNonNull(entity);
	}

	@Redirect (method = "eat",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V"))
	private void fukkit_foodChangeEvent(HungerManager manager, int hunger, float saturation, Item item,
	                                    ItemStack itemStack) {
		int old = this.foodLevel;
		org.bukkit.event.entity.FoodLevelChangeEvent event = org.bukkit.craftbukkit.event.CraftEventFactory
		                                                     .callFoodLevelChangeEvent(this.player, hunger + old,
		                                                     itemStack);

		if (!event.isCancelled()) {
			this.add(event.getFoodLevel() - old, saturation);
		}
		((ServerPlayerEntityAccess) this.player).getBukkit().sendHealthUpdate();
	}

	@Shadow public abstract void add(int food, float f);

	@Redirect (method = "update", at = @At (value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
	private int fukkit_foodLevelChangeEvent(int foodLevelMinusOne, int zero) {
		FoodLevelChangeEvent event = CraftEventFactory
		                             .callFoodLevelChangeEvent(this.player, Math.max(foodLevelMinusOne, 0));
		int level;
		if (!event.isCancelled()) {
			level = event.getFoodLevel();
		} else {
			level = this.foodLevel;
		}
		((ServerPlayerEntity) this.player).networkHandler
		.sendPacket(new HealthUpdateS2CPacket(((ServerPlayerEntityAccess) this.player).getBukkit()
		                                                                              .getScaledHealth(), level,
		this.foodSaturationLevel));
		return level;
	}

	@Redirect (method = "update",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"))
	private void fukkit_healEvent(PlayerEntity entity, float amount) {
		((PlayerEntityAccess<?>) entity).heal(amount, EntityRegainHealthEvent.RegainReason.SATIATED);
	}
}
