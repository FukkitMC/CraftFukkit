package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.border;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.border.WorldBorderAccess;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Implements (@Interface (iface = WorldBorderAccess.class, prefix = "fukkit$"))
@Mixin (WorldBorder.class)
public abstract class WorldBorderMixin {
	public ServerWorld world;
	@Shadow @Final private List<WorldBorderListener> listeners;

	@Inject (method = "addListener(Lnet/minecraft/world/border/WorldBorderListener;)V", at = @At ("HEAD"),
	         cancellable = true)
	public void addListener(WorldBorderListener listener, CallbackInfo ci) {
		if (this.listeners.contains(listener)) { ci.cancel(); }
	}

	public ServerWorld fukkit$getServerWorld() {
		return this.world;
	}

	public void fukkit$setServerWorld(ServerWorld world) {
		this.world = world;
	}
}
