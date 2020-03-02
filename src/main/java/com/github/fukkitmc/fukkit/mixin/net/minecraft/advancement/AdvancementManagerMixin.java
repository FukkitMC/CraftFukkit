package com.github.fukkitmc.fukkit.mixin.net.minecraft.advancement;

import net.minecraft.advancement.AdvancementManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdvancementManager.class)
public class AdvancementManagerMixin {
	@Redirect(method = "load(Ljava/util/Map;)V", at = @At(target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", value = "INVOKE", remap = false))
	public void redirectUnload(Logger logger, String message, Object p0) {
		// CraftBukkit - moved to AdvancementDataWorld#reload
	}
}
