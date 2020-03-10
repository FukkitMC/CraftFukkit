package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (PlantBlock.class)
public class PlantBlockMixin extends Block {
	public PlantBlockMixin(Settings settings) {
		super(settings);
	}

	@Redirect (method = "getStateForNeighborUpdate", at = @At (value = "INVOKE",
	                                                           target = "Lnet/minecraft/block/Block;getDefaultState()" +
	                                                                    "Lnet/minecraft/block/BlockState;"))
	private BlockState fukkit_physicsEvent(Block air, BlockState state, Direction facing, BlockState neighborState,
	                                       IWorld world, BlockPos pos, BlockPos neighborPos) {
		if (!CraftEventFactory.callBlockPhysicsEvent(world, pos).isCancelled()) { return air.getDefaultState(); } else {
			return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
		}
	}
}
