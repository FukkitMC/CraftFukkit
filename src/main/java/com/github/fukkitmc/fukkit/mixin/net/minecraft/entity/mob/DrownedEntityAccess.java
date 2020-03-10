package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.mob;

import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.mob.DrownedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin (DrownedEntity.class)
public interface DrownedEntityAccess {
	@Accessor ("waterNavigation") SwimNavigation getWaterNavigation();

	@Accessor ("landNavigation") MobNavigation getLandNavigation();
}
