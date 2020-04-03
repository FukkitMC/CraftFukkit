package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.task.GoToWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (GoToWorkTask.class)
public class GoToWorkTaskMixin {
	private static Villager.Profession profession;

	@Inject (method = "method_18986",
	         at = @At (value = "INVOKE",
	                   target = "net/minecraft/entity/passive/VillagerEntity.setVillagerData(Lnet/minecraft/village/VillagerData;)V"),
	         cancellable = true)
	private static void fukkit_villagerCarreerEvent(VillagerEntity entity, ServerWorld world, VillagerProfession profession, CallbackInfo ret) {
		VillagerCareerChangeEvent event = CraftEventFactory.callVillagerCareerChangeEvent(entity, CraftVillager.nmsToBukkitProfession(profession), VillagerCareerChangeEvent.ChangeReason.EMPLOYED);
		if (event.isCancelled()) {
			ret.cancel();
		} else GoToWorkTaskMixin.profession = event.getProfession();
	}

	@Redirect (method = "method_18986",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/village/VillagerData;withProfession(Lnet/minecraft/village/VillagerProfession;)Lnet/minecraft/village/VillagerData;"))
	private static VillagerData fukkit_villagerCarreerEvent(VillagerData entity, VillagerProfession villagerData) {
		return entity.withProfession(CraftVillager.bukkitToNmsProfession(profession));
	}
}
