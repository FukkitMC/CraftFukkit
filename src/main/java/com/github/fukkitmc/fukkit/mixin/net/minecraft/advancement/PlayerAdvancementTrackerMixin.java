package com.github.fukkitmc.fukkit.mixin.net.minecraft.advancement;

import com.github.fukkitmc.fukkit.access.CraftHandled;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;

@Mixin (PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
	@Shadow @Final private static Logger LOGGER;

	@Shadow private ServerPlayerEntity owner;

	@Redirect (method = "load()V", at = @At (value = "INVOKE",
	                                         target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;" +
	                                                  "Ljava/lang/Object;Ljava/lang/Object;)V",
	                                         remap = false))
	public void ifModdedThing(Logger logger, String message, Object p0, Object p1) {
		if ("minecraft".equals(((Map.Entry<Identifier, ?>) p0).getKey().getNamespace())) {
			LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", p0, p1);
		}
	}

	@Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void fukkit_playerAdvancementDoneEvent(Advancement advancement, String criterion, CallbackInfoReturnable<Boolean> cir) {
		((WorldAccess)(this.owner.world)).getBukkitServer().getPluginManager().callEvent(new PlayerAdvancementDoneEvent(((ServerPlayerEntityAccess)this.owner).getBukkit(), ((CraftHandled<org.bukkit.advancement.Advancement>)advancement).getBukkit()));
	}
}
