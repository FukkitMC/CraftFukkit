package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Random;

@Mixin (ScaffoldingBlock.class)
public class ScaffoldingBlockMixin {
	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/block/BlockState;get" +
	                                                        "(Lnet/minecraft/state/property/Property;)" +
	                                                        "Ljava/lang/Comparable;",
	                                               ordinal = 0))
	private <T extends Comparable<T>> T fukkit_fadeEvent(BlockState state, Property<Integer> property,
	                                                     BlockState state2, ServerWorld world, BlockPos pos,
	                                                     Random random) {
		int distance = state.get(property);
		if (distance == 7 && !CraftEventFactory.callBlockFadeEvent(world, pos, Blocks.AIR.getDefaultState())
		                                       .isCancelled()) {
			return (T) Integer.valueOf(7); // make if return true
		} else {
			return (T) Integer.valueOf(-1); // make if return false
		}
	}


}
