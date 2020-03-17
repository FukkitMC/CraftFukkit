package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net/minecraft/block/entity/BeehiveBlockEntity$Bee")
public interface BeehiveBlockEntity$BeeAccess {
	@Accessor("ticksInHive") void setTicksInHive(int ticks);
	@Accessor("minOccupationTIcks") int getMinOccupationTicks();
}
