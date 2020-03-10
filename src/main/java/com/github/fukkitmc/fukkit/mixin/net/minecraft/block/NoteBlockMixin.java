package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.NotePlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (NoteBlock.class)
public class NoteBlockMixin {
	@Redirect (method = "neighborUpdate", at = @At (value = "INVOKE",
	                                                target = "Lnet/minecraft/block/NoteBlock;playNote" +
	                                                         "(Lnet/minecraft/world/World;" +
	                                                         "Lnet/minecraft/util/math/BlockPos;)V"))
	private void fukkit_play(NoteBlock block, World world, BlockPos pos, BlockState state, World world1, BlockPos pos1
	, Block block1, BlockPos neighborPos, boolean moved) {
		this.playNote(world, pos, state);
	}

	private void playNote(World world, BlockPos pos, BlockState data) {
		if (world.getBlockState(pos.up()).isAir()) {
			NotePlayEvent event = CraftEventFactory.callNotePlayEvent(world, pos, data.get(NoteBlock.INSTRUMENT), data
			                                                                                                      .get(NoteBlock.NOTE));
			if (!event.isCancelled()) {
				world.addBlockAction(pos, (NoteBlock) (Object) this, 0, 0);
			}
		}
	}

	@Redirect (method = "onUse", at = @At (value = "INVOKE",
	                                       target = "Lnet/minecraft/block/NoteBlock;playNote" +
	                                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)" +
	                                                "V"))
	private void fukkit_play(NoteBlock block, World world, BlockPos pos, BlockState state, World world2, BlockPos pos2
	, PlayerEntity player, Hand hand, BlockHitResult hit) {
		this.playNote(world, pos, state);
	}

	@Redirect (method = "onBlockBreakStart", at = @At (value = "INVOKE",
	                                                   target = "Lnet/minecraft/block/NoteBlock;playNote" +
	                                                            "(Lnet/minecraft/world/World;" +
	                                                            "Lnet/minecraft/util/math/BlockPos;)V"))
	private void fukkit_break(NoteBlock block, World world, BlockPos pos, BlockState state, World world2,
	                          BlockPos pos2, PlayerEntity player) {
		this.playNote(world, pos, state);
	}

	@Redirect (method = "playNote", at = @At (value = "INVOKE",
	                                          target = "Lnet/minecraft/world/World;addBlockAction" +
	                                                   "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;" +
	                                                   "II)V"))
	private void fukkit_notePlayEvent(World world, BlockPos pos, Block block, int type, int data) {
		this.playNote(world, pos, world.getBlockState(pos)); // mod compat
	}
}
