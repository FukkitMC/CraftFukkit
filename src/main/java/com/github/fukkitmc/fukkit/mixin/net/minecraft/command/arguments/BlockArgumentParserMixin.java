package com.github.fukkitmc.fukkit.mixin.net.minecraft.command.arguments;

import com.mojang.brigadier.StringReader;
import net.minecraft.command.arguments.BlockArgumentParser;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(BlockArgumentParser.class)
public class BlockArgumentParserMixin {
	@Mutable
	@Shadow @Final private Map<Property<?>, Comparable<?>> blockProperties;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void postInit(StringReader reader, boolean allowTag, CallbackInfo ci) {
		this.blockProperties = new LinkedHashMap<>();
	}
}
