package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin (ChorusFlowerBlock.class)
public class ChorusFlowerBlockMixin extends Block {
	public ChorusFlowerBlockMixin(Settings settings) {
		super(settings);
	}

	@Inject (method = "scheduledTick", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                      "(Lnet/minecraft/util/math/BlockPos;" +
	                                                      "Lnet/minecraft/block/BlockState;I)Z",
	                                             ordinal = 0))
	private void fukkit_spreadEvent(BlockState state, ServerWorld world, BlockPos pos, Random random,
	                                CallbackInfo ci) {
		if (!CraftEventFactory.handleBlockSpreadEvent(world, pos, pos.up(), this.getDefaultState()
		                                                                        .with(ChorusFlowerBlock.AGE, state
		                                                                                                     .get(ChorusFlowerBlock.AGE)), 2)) {
			ci.cancel();
		}
	}

	// TODO finish implementation
}
