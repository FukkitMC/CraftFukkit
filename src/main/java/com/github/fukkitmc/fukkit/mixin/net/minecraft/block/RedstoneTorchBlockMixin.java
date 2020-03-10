package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin (RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin {
	@Inject (method = "update", at = @At (value = "INVOKE",
	                                      target = "Lnet/minecraft/world/World;setBlockState" +
	                                               "(Lnet/minecraft/util/math/BlockPos;" +
	                                               "Lnet/minecraft/block/BlockState;I)Z",
	                                      ordinal = 0), cancellable = true)
	private static void fukkit_noPower(BlockState state, World world, BlockPos pos, Random random, boolean unpower,
	                                   CallbackInfo ci) {
		if (redstoneEvent(world, pos, state, 0)) { ci.cancel(); }
	}

	/**
	 * this does not imitate Bukkit exactly, but it should have the same effect
	 *
	 * @return true if method should exit
	 */
	private static boolean redstoneEvent(World world, BlockPos pos, BlockState state, int expecting) {
		org.bukkit.plugin.PluginManager manager = ((WorldAccess) world).getBukkitServer().getPluginManager();
		org.bukkit.block.Block block = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(),
		pos.getZ());
		int oldCurrent = state.get(RedstoneTorchBlock.LIT) ? 15 : 0;
		BlockRedstoneEvent event = new BlockRedstoneEvent(block, oldCurrent, oldCurrent);
		if (oldCurrent != expecting) {
			event.setNewCurrent(expecting);
			manager.callEvent(event);
			return event.getNewCurrent() != expecting;
		}
		return false;
	}

	@Inject (method = "update", at = @At (value = "INVOKE",
	                                      target = "Lnet/minecraft/world/World;setBlockState" +
	                                               "(Lnet/minecraft/util/math/BlockPos;" +
	                                               "Lnet/minecraft/block/BlockState;I)Z",
	                                      ordinal = 0), cancellable = true)
	private static void fukkit_noPower2(BlockState state, World world, BlockPos pos, Random random, boolean unpower,
	                                    CallbackInfo ci) {
		if (redstoneEvent(world, pos, state, 15)) { ci.cancel(); }
	}
}
