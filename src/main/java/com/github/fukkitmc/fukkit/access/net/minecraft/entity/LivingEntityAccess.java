package com.github.fukkitmc.fukkit.access.net.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.w3c.dom.Entity;
import java.util.ArrayList;

public interface LivingEntityAccess<E extends CraftLivingEntity> extends EntityAccess<E> {
	int getExpToDrop();
	int getMaxAirTicks();
	boolean shouldForceDrops();
	ArrayList<ItemStack> getDrops();
	CraftAttributeMap getCraftAttributes();
	boolean collides();
	boolean canPickUpLoot();
	void setExpToDrop(int exp);
	void setMaxAirTicks(int ticks);
	void setForceDrops(boolean forceDrops);
	void setCraftAttributes(CraftAttributeMap map);
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
