package com.github.fukkitmc.fukkit.mixin.net.minecraft.enchantment;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.LivingEntityAccess;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin {
	@Redirect(method = "onTargetDamaged", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"))
	private boolean fukkit_cause(LivingEntity entity, StatusEffectInstance effect) {
		return ((LivingEntityAccess<?>)entity).addEffect(effect, EntityPotionEffectEvent.Cause.ATTACK);
	}
}
