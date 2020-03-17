package com.github.fukkitmc.fukkit.access.net.minecraft.block.entity;

import net.minecraft.entity.player.PlayerEntity;
import org.bukkit.potion.PotionEffect;
import java.util.List;

public interface BeaconBlockEntityAccess {
	PotionEffect getPrimaryEffect();
	PotionEffect getSecondaryEffect();
	List<PlayerEntity> getHumansInRange();
}
