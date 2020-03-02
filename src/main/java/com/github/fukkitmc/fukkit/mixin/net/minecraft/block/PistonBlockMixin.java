package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.block.DualBlockList;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.List;
import java.util.Map;

@Mixin (PistonBlock.class)
public class PistonBlockMixin {
	@Shadow
	@Final
	private boolean isSticky;

	@Inject (method = "tryMove", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void fukkit_sticky(World world, BlockPos pos, BlockState state, CallbackInfo ci, Direction direction) {
		if (!this.isSticky) {
			Block block = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
			BlockPistonRetractEvent event = new BlockPistonRetractEvent(block, ImmutableList.of(), CraftBlock.notchToBlockFace(direction));
			((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				ci.cancel();
			}
		}
	}

	@Inject (method = "move", at = @At (value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;getOpposite()Lnet/minecraft/util/math/Direction;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_pistonMoveEvent(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos pos1, PistonHandler handler) {
		Direction direction = retract ? dir : dir.getOpposite();
		final org.bukkit.block.Block bblock = ((WorldAccess)world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());

		final List<BlockPos> moved = handler.getMovedBlocks();
		final List<BlockPos> broken = handler.getBrokenBlocks();

		List<org.bukkit.block.Block> blocks = new DualBlockList(moved, broken, bblock.getWorld());
		org.bukkit.event.block.BlockPistonEvent event;
		if (retract) {
			event = new BlockPistonExtendEvent(bblock, blocks, CraftBlock.notchToBlockFace(direction));
		} else {
			event = new BlockPistonRetractEvent(bblock, blocks, CraftBlock.notchToBlockFace(direction));
		}
		((WorldAccess)world).getBukkitServer().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			for (BlockPos broke : broken) {
				world.updateListeners(broke, Blocks.AIR.getDefaultState(), world.getBlockState(broke), 3);
			}
			for (BlockPos move : moved) {
				world.updateListeners(move, Blocks.AIR.getDefaultState(), world.getBlockState(move), 3);
				move = move.offset(direction);
				world.updateListeners(move, Blocks.AIR.getDefaultState(), world.getBlockState(move), 3);
			}
			cir.setReturnValue(false);
		}
	}
}
