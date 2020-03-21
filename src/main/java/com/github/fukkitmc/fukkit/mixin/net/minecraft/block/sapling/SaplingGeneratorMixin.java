package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.sapling;

import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.bukkit.TreeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Random;

@Mixin(SaplingGenerator.class)
public class SaplingGeneratorMixin {
	@Inject(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_setTreeType(IWorld iWorld, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random, CallbackInfoReturnable<Boolean> cir, ConfiguredFeature<?, ?> configuredFeature) {
		this.setTreeType(configuredFeature);
	}

	protected void setTreeType(ConfiguredFeature<?, ?> feature) {
		if (feature.config == DefaultBiomeFeatures.OAK_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.TREE;
		} else if (feature.config == DefaultBiomeFeatures.HUGE_RED_MUSHROOM_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.RED_MUSHROOM;
		} else if (feature.config == DefaultBiomeFeatures.HUGE_BROWN_MUSHROOM_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.BROWN_MUSHROOM;
		} else if (feature.config == DefaultBiomeFeatures.JUNGLE_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.COCOA_TREE;
		} else if (feature.config == DefaultBiomeFeatures.JUNGLE_SAPLING_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.SMALL_JUNGLE;
		} else if (feature.config == DefaultBiomeFeatures.PINE_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.TALL_REDWOOD;
		} else if (feature.config == DefaultBiomeFeatures.SPRUCE_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.REDWOOD;
		} else if (feature.config == DefaultBiomeFeatures.ACACIA_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.ACACIA;
		} else if (feature.config == DefaultBiomeFeatures.BIRCH_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.BIRCH;
		} else if (feature.config == DefaultBiomeFeatures.LARGE_BIRCH_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.TALL_BIRCH;
		} else if (feature.config == DefaultBiomeFeatures.SWAMP_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.SWAMP;
		} else if (feature.config == DefaultBiomeFeatures.FANCY_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.BIG_TREE;
		} else if (feature.config == DefaultBiomeFeatures.JUNGLE_GROUND_BUSH_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.JUNGLE_BUSH;
		} else if (feature.config == DefaultBiomeFeatures.DARK_OAK_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.DARK_OAK;
		} else if (feature.config == DefaultBiomeFeatures.MEGA_SPRUCE_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.MEGA_REDWOOD;
		} else if (feature.config == DefaultBiomeFeatures.MEGA_PINE_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.MEGA_REDWOOD;
		} else if (feature.config == DefaultBiomeFeatures.MEGA_JUNGLE_TREE_CONFIG) {
			Constants.saplingBlockTreeType = TreeType.JUNGLE;
		} else {
			throw new IllegalArgumentException("Unknown tree generator " + feature);
		}
	}
}
