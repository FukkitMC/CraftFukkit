package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Random;

@Mixin (FireBlock.class)
public abstract class FireBlockMixin {
	@Shadow
	@Final
	public static IntProperty AGE;

	@Redirect (method = "getStateForNeighborUpdate", at = @At (value = "INVOKE",
	                                                           target = "Lnet/minecraft/block/FireBlock;canPlaceAt" +
	                                                                    "(Lnet/minecraft/block/BlockState;" +
	                                                                    "Lnet/minecraft/world/WorldView;" +
	                                                                    "Lnet/minecraft/util/math/BlockPos;)Z"))
	private boolean fukkit_stateCan(FireBlock block, BlockState state, WorldView world, BlockPos pos) {
		return state.canPlaceAt(world, pos);
	}

	@Redirect (method = "getStateForNeighborUpdate", at = @At (value = "INVOKE",
	                                                           target = "Lnet/minecraft/block/Block;getDefaultState()" +
	                                                                    "Lnet/minecraft/block/BlockState;"))
	private BlockState fukkit_fadeEvent(Block block, BlockState state, Direction facing, BlockState neighborState,
	                                    IWorld world, BlockPos pos, BlockPos neighborPos) {
		CraftBlockState blockState = CraftBlockState.getBlockState(world, pos);
		blockState.setData(Blocks.AIR.getDefaultState());

		BlockFadeEvent event = new BlockFadeEvent(blockState.getBlock(), blockState);
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			return blockState.getHandle();
		}
		return this.getStateForPosition(world, pos).with(AGE, state.get(AGE));
	}

	@Shadow
	public abstract BlockState getStateForPosition(BlockView world, BlockPos pos);

	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;removeBlock" +
	                                                        "(Lnet/minecraft/util/math/BlockPos;Z)Z"))
	private boolean fukkit_removeBlock(ServerWorld world, BlockPos pos, boolean move) {
		fireExtinguished(world, pos);
		return false; // return type is not used
	}

	@Unique
	private static void fireExtinguished(IWorld world, BlockPos position) {
		if (!CraftEventFactory.callBlockFadeEvent(world, position, Blocks.AIR.getDefaultState()).isCancelled()) {
			world.removeBlock(position, false);
		}
	}

	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/block/FireBlock;trySpreadingFire" +
	                                                        "(Lnet/minecraft/world/World;" +
	                                                        "Lnet/minecraft/util/math/BlockPos;ILjava/util/Random;I)" +
	                                                        "V"))
	private void fukkit_addSource(FireBlock block, World world, BlockPos pos, int spreadFactor, Random rand,
	                              int currentAge, BlockState state, ServerWorld world2, BlockPos initial,
	                              Random random2) {
		// CraftBukkit add sourceposition
		int spreadChance = this.getSpreadChance(world.getBlockState(pos));

		if (random2.nextInt(spreadFactor) < spreadChance) {
			BlockState currentState = world.getBlockState(pos);

			// CraftBukkit start
			org.bukkit.block.Block theBlock = ((WorldAccess) world).getBukkit()
			                                                       .getBlockAt(pos.getX(), pos.getY(), pos.getZ());
			org.bukkit.block.Block sourceBlock = ((WorldAccess) world).getBukkit().getBlockAt(initial.getX(), initial
			                                                                                                  .getY(),
			initial
			                                                                                                           .getZ());

			BlockBurnEvent event = new BlockBurnEvent(theBlock, sourceBlock);
			((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return;
			}
			// CraftBukkit end

			if (rand.nextInt(currentAge + 10) < 5 && !world.hasRain(pos)) {
				int l = Math.min(currentAge + random2.nextInt(5) / 4, 15);

				world.setBlockState(pos, this.getStateForPosition(world, pos).with(FireBlock.AGE, l), 3);
			} else {
				world.removeBlock(pos, false);
			}

			Block currentBlock = currentState.getBlock();

			if (currentBlock instanceof TntBlock) {
				TntBlock.primeTnt(world, pos);
			}
		}

	}

	@Shadow protected abstract int getSpreadChance(BlockState state);

	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                        "(Lnet/minecraft/util/math/BlockPos;" +
	                                                        "Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_callToStopSpread(ServerWorld world, BlockPos targetPosition, BlockState state, int flags,
	                                        BlockState state2, ServerWorld world2, BlockPos currentPos,
	                                        Random random2) {
		if (world.getBlockState(targetPosition).getBlock() != Blocks.FIRE) {
			if (!CraftEventFactory.callBlockIgniteEvent(world, targetPosition, currentPos).isCancelled()) {
				CraftEventFactory.handleBlockSpreadEvent(world, currentPos, targetPosition, state, flags);
			}
		}
		return false;
	}

	@Redirect (method = "onBlockAdded", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/world/dimension/Dimension;getType()" +
	                                                       "Lnet/minecraft/world/dimension/DimensionType;"))
	private DimensionType fukkit_md5pls(Dimension dimension) {
		return ((DimensionTypeAccess) dimension.getType()).getType();
	}

	@Redirect (method = "onBlockAdded", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
	private boolean fukkit_fuelBlockBroke(World world, BlockPos pos, boolean move) {
		fireExtinguished(world, pos);
		return move;
	}
}
