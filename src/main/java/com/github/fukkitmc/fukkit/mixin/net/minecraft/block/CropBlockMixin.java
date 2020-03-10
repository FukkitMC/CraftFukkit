package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (CropBlock.class)
public class CropBlockMixin extends PlantBlock {
	protected CropBlockMixin(Settings settings) {
		super(settings);
	}

	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                        "(Lnet/minecraft/util/math/BlockPos;" +
	                                                        "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_growEvent0(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
	}

	@Redirect (method = "applyGrowth", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/world/World;setBlockState" +
	                                                      "(Lnet/minecraft/util/math/BlockPos;" +
	                                                      "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_growEvent1(World world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
	}

	@Redirect (method = "onEntityCollision", at = @At (value = "INVOKE",
	                                                   target = "Lnet/minecraft/world/GameRules;getBoolean" +
	                                                            "(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
	private boolean fukkit_ifHack(GameRules rules, GameRules.RuleKey<GameRules.BooleanRule> rule) { return true;
	/*mumfrey pls allow us to add conditions to ifs*/ }

	@Inject (method = "onEntityCollision", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/world/World;breakBlock" +
	                                                          "(Lnet/minecraft/util/math/BlockPos;" +
	                                                          "ZLnet/minecraft/entity/Entity;)Z"))
	private void fukkit_entityChangeBlockEvent(BlockState state, World world, BlockPos pos, Entity entity,
	                                           CallbackInfo ci) {
		if (CraftEventFactory
		    .callEntityChangeBlockEvent(entity, pos, Blocks.AIR.getDefaultState(), !world.getGameRules()
		                                                                                 .getBoolean(GameRules.MOB_GRIEFING))
		    .isCancelled()) {
			super.onEntityCollision(state, world, pos, entity);
			ci.cancel();
		}
	}
}
