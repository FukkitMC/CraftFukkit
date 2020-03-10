package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (TallPlantBlock.class)
public class TallPlantBlockMixin {
	@Inject (method = "onBreak", at = @At ("HEAD"), cancellable = true)
	private void fukkit_onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
		if (CraftEventFactory.callBlockPhysicsEvent(world, pos).isCancelled()) { ci.cancel(); }
	}
}
