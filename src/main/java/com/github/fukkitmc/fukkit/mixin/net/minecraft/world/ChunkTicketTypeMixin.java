package com.github.fukkitmc.fukkit.mixin.net.minecraft.world;

import net.minecraft.server.world.ChunkTicketType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/* NOTICE: This is an empty mixin and handled by CraftFukkitMixinPlugin
 * This is because Mixin does not allow me to add public static field in Mixin.
 */
@Mixin(ChunkTicketType.class)
public class ChunkTicketTypeMixin {
	@Shadow
	@Mutable
	public long expiryTicks;
}
