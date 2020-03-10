package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;

@Mixin (MushroomPlantBlock.class)
public class MushroomPlantBlockMixin {
	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                        "(Lnet/minecraft/util/math/BlockPos;" +
	                                                        "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_spreadEvent(ServerWorld world, BlockPos pos2, BlockState state, int flags,
	                                   BlockState state2, ServerWorld world2, BlockPos pos, Random random) {
		return CraftEventFactory.handleBlockSpreadEvent(world, pos, pos2, state, flags);
	}

	@Inject (method = "trySpawningBigMushroom", at = @At (value = "INVOKE",
	                                                      target = "Lnet/minecraft/world/gen/feature/Feature;configure" +
	                                                               "(Lnet/minecraft/world/gen/feature/FeatureConfig;)" +
	                                                               "Lnet/minecraft/world/gen/feature" +
	                                                               "/ConfiguredFeature;",
	                                                      ordinal = 0))
	private void fukkit_brown(ServerWorld serverWorld, BlockPos pos, BlockState state, Random random,
	                          CallbackInfoReturnable<Boolean> cir) {
		Constants.saplingBlockTreeType = TreeType.BROWN_MUSHROOM;
	}

	@Inject (method = "trySpawningBigMushroom", at = @At (value = "INVOKE",
	                                                      target = "Lnet/minecraft/world/gen/feature/Feature;configure" +
	                                                               "(Lnet/minecraft/world/gen/feature/FeatureConfig;)" +
	                                                               "Lnet/minecraft/world/gen/feature/ConfiguredFeature;",
	                                                      ordinal = 1))
	private void fukkit_red(ServerWorld serverWorld, BlockPos pos, BlockState state, Random random, CallbackInfoReturnable<Boolean> cir) {
		Constants.saplingBlockTreeType = TreeType.RED_MUSHROOM;
	}
}
