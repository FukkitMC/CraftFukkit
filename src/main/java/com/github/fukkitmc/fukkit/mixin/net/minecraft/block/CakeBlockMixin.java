package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {
	@Inject(method = "tryEat", at = @At(target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V", value = "INVOKE"))
	private void fukkit_foodEvent(IWorld world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<ActionResult> cir) {
		HungerManager manager = player.getHungerManager();
		int old = manager.getFoodLevel();
		FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(player, 2 + old);
		if(!event.isCancelled())
			manager.add(event.getFoodLevel() - old, .1f);
		((CraftPlayer)((EntityAccess)player).getBukkit()).sendHealthUpdate();
	}

	@Redirect (method = "tryEat", at = @At(target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V", value = "INVOKE"))
	private void fukkit_nullifyEat(HungerManager manager, int food, float f) {

	}
}
