package com.github.fukkitmc.fukkit.mixin.net.minecraft.advancement;

import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Map;

@Mixin (PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
	@Shadow @Final private static Logger LOGGER;

	@Redirect (method = "load()V", at = @At (value = "INVOKE",
	                                         target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;" +
	                                                  "Ljava/lang/Object;Ljava/lang/Object;)V",
	                                         remap = false))
	public void ifModdedThing(Logger logger, String message, Object p0, Object p1) {
		if ("minecraft".equals(((Map.Entry<Identifier, ?>) p0).getKey().getNamespace())) {
			LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", p0, p1);
		}
	}
}
