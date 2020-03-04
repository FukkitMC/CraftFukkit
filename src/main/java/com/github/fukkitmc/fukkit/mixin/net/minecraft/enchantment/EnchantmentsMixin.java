package com.github.fukkitmc.fukkit.mixin.net.minecraft.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (Enchantments.class)
public class EnchantmentsMixin {
	@Inject (method = "register(Ljava/lang/String;Lnet/minecraft/enchantment/Enchantment;)Lnet/minecraft/enchantment/Enchantment;", at = @At ("TAIL"))
	private static void fukkit_register(String string, Enchantment enchantment, CallbackInfoReturnable<Enchantment> cir) {
		org.bukkit.enchantments.Enchantment.registerEnchantment(new CraftEnchantment(enchantment));
	}
}
