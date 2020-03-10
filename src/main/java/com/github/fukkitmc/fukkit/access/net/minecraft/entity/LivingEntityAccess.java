package com.github.fukkitmc.fukkit.access.net.minecraft.entity;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;

public interface LivingEntityAccess<E extends CraftLivingEntity> extends EntityAccess<E> {
	int getExpToDrop();

	void setExpToDrop(int exp);

	int getMaxAirTicks();

	void setMaxAirTicks(int ticks);

	boolean shouldForceDrops();

	ArrayList<ItemStack> getDrops();

	CraftAttributeMap getCraftAttributes();

	void setCraftAttributes(CraftAttributeMap map);

	boolean collides();

	boolean canPickUpLoot();

	void setForceDrops(boolean forceDrops);

	void setCollides(boolean collides);

	void setCanPickUpLoot(boolean canPickUpLoot);

	boolean removeAllEffects(EntityPotionEffectEvent.Cause cause);

	boolean addEffect(StatusEffectInstance instance, EntityPotionEffectEvent.Cause cause);

	StatusEffectInstance removeStatusEffectInternal(StatusEffect statusEffect, EntityPotionEffectEvent.Cause cause);

	boolean removeEffect(StatusEffect effect, EntityPotionEffectEvent.Cause cause);

	void heal(float f, EntityRegainHealthEvent.RegainReason regainReason);

	int getExpReward();

	boolean damage0(DamageSource source, float damage);
}
