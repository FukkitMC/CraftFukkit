package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Random;

@Mixin (AbstractRedstoneGateBlock.class)
public class AbstractRedstoneGateBlockMixin {
	@Inject (method = "scheduledTick", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                      "(Lnet/minecraft/util/math/BlockPos;" +
	                                                      "Lnet/minecraft/block/BlockState;I)Z"),
	         locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void fukkit_redstoneChange(BlockState state, ServerWorld world, BlockPos pos, Random random,
	                                  CallbackInfo ci, boolean bl, boolean bl2) {
		// we inject into both because we can :yeefuckinhaw:
		int num = bl ? 15 : 0;
		int inverse = bl ? 0 : 15;
		if (CraftEventFactory.callRedstoneChange(world, pos, num, bl ? 0 : 15).getNewCurrent() != inverse) {
			ci.cancel();
		}
	}
}
