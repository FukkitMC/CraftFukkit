package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(SnowBlock.class)
public class SnowBlockMixin {
	@Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/SnowBlock;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"), cancellable = true)
	private void fukkit_fadeEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if(CraftEventFactory.callBlockFadeEvent(world, pos, Blocks.AIR.getDefaultState()).isCancelled())
			ci.cancel();
	}
}
