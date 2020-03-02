package com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.entity;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class EntityUtil {
	public static boolean isLevelAtLeast(CompoundTag tag, int level) {
		return tag.contains("Bukkit.updateLevel") && tag.getInt("Bukkit.updateLevel") >= level;
	}

	public static class ProcessableEffect {
		private StatusEffect type;
		private StatusEffectInstance effect;
		private final EntityPotionEffectEvent.Cause cause;

		public ProcessableEffect(StatusEffectInstance effect, EntityPotionEffectEvent.Cause cause) {
			this.effect = effect;
			this.cause = cause;
		}

		public ProcessableEffect(StatusEffect type, EntityPotionEffectEvent.Cause cause) {
			this.type = type;
			this.cause = cause;
		}

		public StatusEffect getType() {
			return this.type;
		}

		public StatusEffectInstance getEffect() {
			return this.effect;
		}

		public EntityPotionEffectEvent.Cause getCause() {
			return this.cause;
		}
	}
}
