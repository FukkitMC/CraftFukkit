package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.sapling;

import net.minecraft.block.BlockState;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Random;

@Mixin(LargeTreeSaplingGenerator.class)
public class LargeTreeSaplingGeneratorMixin extends SaplingGeneratorMixin {
	@Inject (method = "generateLargeTree", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_setTreeType(IWorld iWorld, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random, int i, int j, CallbackInfoReturnable<Boolean> cir, ConfiguredFeature<?, ?> configuredFeature) {
		this.setTreeType(configuredFeature);
	}
}
