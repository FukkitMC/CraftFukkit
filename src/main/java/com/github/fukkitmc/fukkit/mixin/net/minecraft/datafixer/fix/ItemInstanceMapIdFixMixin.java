package com.github.fukkitmc.fukkit.mixin.net.minecraft.datafixer.fix;

import com.mojang.datafixers.Dynamic;
import net.minecraft.datafixer.fix.ItemInstanceMapIdFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings ("UnresolvedMixinReference")
@Mixin (ItemInstanceMapIdFix.class)
public class ItemInstanceMapIdFixMixin {
	@Redirect (
	method = "method_5032(Lcom/mojang/datafixers/OpticFinder;Lcom/mojang/datafixers/OpticFinder;" +
	         "Lcom/mojang/datafixers/Typed;)Lcom/mojang/datafixers/Typed;",
	at = @At (value = "INVOKE",
	          target = "Lcom/mojang/datafixers/Dynamic;set(Ljava/lang/String;Lcom/mojang/datafixers/Dynamic;)" +
	                   "Lcom/mojang/datafixers/Dynamic;",
	          remap = false), remap = false)
	private static Dynamic<?> set(Dynamic<?> dynamic, String key, Dynamic<?> value) {
		if (!dynamic.getElement("map").isPresent()) {
			return dynamic.set("map", dynamic.createInt(dynamic.get("Damage").asInt(0)));
		}
		return dynamic;
	}
}
