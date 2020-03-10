package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.event.world.StructureGrowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
import java.util.Random;

@Mixin (SaplingBlock.class)
public class SaplingBlockMixin {
	@Inject (method = "scheduledTick", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/block/SaplingBlock;generate" +
	                                                      "(Lnet/minecraft/server/world/ServerWorld;" +
	                                                      "Lnet/minecraft/util/math/BlockPos;" +
	                                                      "Lnet/minecraft/block/BlockState;Ljava/util/Random;)V"))
	private void fukkit_captureGen(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		((WorldAccess) world).setCaptureTreeGen(true);
	}

	@Inject (method = "scheduledTick", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/block/SaplingBlock;generate" +
	                                                      "(Lnet/minecraft/server/world/ServerWorld;" +
	                                                      "Lnet/minecraft/util/math/BlockPos;" +
	                                                      "Lnet/minecraft/block/BlockState;Ljava/util/Random;)V",
	                                             shift = At.Shift.AFTER))
	private void fukkit_captureGen2(BlockState state, ServerWorld world, BlockPos pos, Random random,
	                                CallbackInfo ci) {
		if (!((WorldAccess) world).getCapturedStates().isEmpty()) {
			TreeType treeType = Constants.saplingBlockTreeType;
			Constants.saplingBlockTreeType = null;
			Location location = new Location(((WorldAccess) world).getBukkit(), pos.getX(), pos.getY(), pos.getZ());
			List<org.bukkit.block.BlockState> blocks = (List<org.bukkit.block.BlockState>) ((WorldAccess) world)
			                                                                               .getCapturedStates().clone();
			((WorldAccess) world).getCapturedStates().clear();
			StructureGrowEvent event = null;
			if (treeType != null) {
				event = new StructureGrowEvent(location, treeType, false, null, blocks);
				org.bukkit.Bukkit.getPluginManager().callEvent(event);
			}
			if (event == null || !event.isCancelled()) {
				for (org.bukkit.block.BlockState blockstate : blocks) {
					blockstate.update(true);
				}
			}
		}
	}
}
