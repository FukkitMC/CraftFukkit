package com.github.fukkitmc.fukkit.mixin.net.minecraft.command;

import com.github.fukkitmc.fukkit.access.net.minecraft.command.arguments.EntitySelectorReaderAccess;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.command.EntitySelectorReader.*;

@Mixin (EntitySelectorReader.class)
public abstract class EntitySelectorReaderMixin implements EntitySelectorReaderAccess {

	@Shadow public abstract EntitySelector read() throws CommandSyntaxException;

	@Shadow
	private boolean usesAt;
	@Shadow protected abstract void readAtVariable() throws CommandSyntaxException;

	private static final ThreadLocal<Boolean> OVERRIDE_PERMISSIONS = ThreadLocal.withInitial(() -> false);

	@Inject(method = "readAtVariable", at = @At("HEAD"))
	private void fukkit_parse(CallbackInfo ci) {
		this.usesAt = !OVERRIDE_PERMISSIONS.get();
	}

	@Inject(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/EntitySelectorReader;readAtVariable()V"))
	private void fukkit_parseSelector(CallbackInfoReturnable<EntitySelector> cir) throws CommandSyntaxException {
		this.usesAt = !OVERRIDE_PERMISSIONS.get();
		this.readAtVariable();
		OVERRIDE_PERMISSIONS.set(false);
	}

	@Override
	public EntitySelector parse(boolean overridePermissions) throws CommandSyntaxException {
		OVERRIDE_PERMISSIONS.set(overridePermissions);
		return this.read();
	}
}
