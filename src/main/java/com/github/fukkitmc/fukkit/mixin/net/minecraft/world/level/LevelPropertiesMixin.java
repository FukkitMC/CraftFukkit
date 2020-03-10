package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.level;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.level.LevelPropertiesAccess;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelProperties;
import org.bukkit.Bukkit;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Implements (@Interface (iface = LevelPropertiesAccess.class, prefix = "fukkit$"))
@Mixin (LevelProperties.class)
public abstract class LevelPropertiesMixin {
	public ServerWorld world;
	@Shadow
	private boolean thundering;
	@Shadow
	private boolean raining;
	@Shadow private String levelName;

	@Inject (
	method = "<init>(Lnet/minecraft/nbt/CompoundTag;Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;" +
	         ")V",
	at = @At ("TAIL"))
	public void init(CompoundTag compoundTag, DataFixer dataFixer, int i, CompoundTag compoundTag2, CallbackInfo ci) {
		compoundTag
		.putString("Bukkit.Version", Bukkit.getName() + "/" + Bukkit.getVersion() + "/" + Bukkit.getBukkitVersion());
	}

	@Inject (method = "setThundering", at = @At ("HEAD"), cancellable = true)
	public void thunder(boolean thundering, CallbackInfo ci) {
		if (this.thundering == thundering) {
			return;
		}

		org.bukkit.World world = Bukkit.getWorld(this.getLevelName());
		if (world != null) {
			ThunderChangeEvent thunder = new ThunderChangeEvent(world, thundering);
			Bukkit.getServer().getPluginManager().callEvent(thunder);
			if (thunder.isCancelled()) { ci.cancel(); }
		}
	}

	@Shadow
	public abstract String getLevelName();

	@Inject (method = "setRaining", at = @At ("HEAD"), cancellable = true)
	public void raining(boolean raining, CallbackInfo ci) {
		if (this.raining == raining) {
			return;
		}

		org.bukkit.World world = Bukkit.getWorld(this.getLevelName());
		if (world != null) {
			WeatherChangeEvent weather = new WeatherChangeEvent(world, raining);
			Bukkit.getServer().getPluginManager().callEvent(weather);
			if (weather.isCancelled()) {
				ci.cancel();
			}
		}
	}

	@Inject (method = "setDifficulty", at = @At ("HEAD"), cancellable = true)
	public void difficulty(Difficulty difficulty, CallbackInfo ci) {
		DifficultyS2CPacket packet = new DifficultyS2CPacket(this.getDifficulty(), this.isDifficultyLocked());
		for (ServerPlayerEntity player : this.world.getPlayers()) {
			player.networkHandler.sendPacket(packet);
		}
	}

	@Shadow public abstract Difficulty getDifficulty();

	@Shadow public abstract boolean isDifficultyLocked();

	public void fukkit$checkName(String name) {
		if (!this.levelName.equals(name)) { this.levelName = name; }
	}

	public ServerWorld fukkit$getServerWorld() {
		return this.world;
	}

	public void fukkit$setServerWorld(ServerWorld world) {
		this.world = world;
	}
}
