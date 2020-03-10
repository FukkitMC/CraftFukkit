package com.github.fukkitmc.fukkit.mixin.net.minecraft.datafixer.fix;

import com.mojang.datafixers.Dynamic;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (ItemInstanceTheFlatteningFix.class)
public class ItemInstanceTheFlatteningFixMixin {
	@Redirect (
	method = "method_5044(Lcom/mojang/datafixers/OpticFinder;Lcom/mojang/datafixers/OpticFinder;" +
	         "Lcom/mojang/datafixers/Typed;)Lcom/mojang/datafixers/Typed;",
	at = @At (value = "INVOKE",
	          target = "Lcom/mojang/datafixers/Dynamic;set(Ljava/lang/String;Lcom/mojang/datafixers/Dynamic;)" +
	                   "Lcom/mojang/datafixers/Dynamic;"),
	remap = false)
	private static Dynamic<?> set(Dynamic<?> dynamic, String value, Dynamic<?> intVal) {
		if (intVal.asInt(0) != 0) { return dynamic.set(value, intVal); }
		return dynamic;
	}
}
