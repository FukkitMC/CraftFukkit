package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.minecraft.block.Block.dropStack;
import static net.minecraft.block.Block.dropStacks;

@Mixin (SpongeBlock.class)
public class SpongeBlockMixin {
	private Queue<BlockStateListPopulator> stateLists = new LinkedList<>();

	@Inject (method = "absorbWater", at = @At ("HEAD"))
	private void fukkit_addStatePop(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		this.stateLists.add(new BlockStateListPopulator(world));
	}

	@Redirect (method = "absorbWater", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private BlockState fukkit_getState(World world, BlockPos pos) {
		return this.stateLists.peek().getBlockState(pos);
	}

	@Redirect (method = "absorbWater", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
	private FluidState fukkit_getFluidState(World world, BlockPos pos) {
		return this.stateLists.peek().getFluidState(pos);
	}

	@Redirect (method = "absorbWater", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/FluidDrainable;tryDrainFluid(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/fluid/Fluid;"))
	private Fluid fukkit_drainList(FluidDrainable drainable, IWorld world, BlockPos pos, BlockState state) {
		return drainable.tryDrainFluid(this.stateLists.peek(), pos, state);
	}

	// two birds 1 stone
	@Redirect (method = "absorbWater", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_blockList(World world, BlockPos pos, BlockState state, int flags) {
		return this.stateLists.peek().setBlockState(pos, state, flags);
	}

	@Redirect (method = "absorbWater", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/Block;hasBlockEntity()Z"))
	private boolean fukkit_false(Block block) {
		return false;
	}

	@Redirect (method = "absorbWater", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/SpongeBlock;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V"))
	private void fukkit_doNothing(BlockState state, World world, BlockPos pos, BlockEntity blockEntity) {

	}

	@Inject (method = "absorbWater", at = @At (value = "TAIL"), cancellable = true)
	private void fukkit_spongeAbsorbEvent(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		List<CraftBlockState> blocks = this.stateLists.peek().getList(); // Is a clone
		if (!blocks.isEmpty()) {
			final org.bukkit.block.Block bblock = ((WorldAccess)world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());

			SpongeAbsorbEvent event = new SpongeAbsorbEvent(bblock, (List<org.bukkit.block.BlockState>) (List) blocks);
			((WorldAccess)world).getBukkitServer().getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				cir.setReturnValue(false);
				return;
			}

			for (CraftBlockState block : blocks) {
				BlockPos blockposition2 = block.getPosition();
				BlockState iblockdata = world.getBlockState(blockposition2);
				FluidState fluid = world.getFluidState(blockposition2);
				Material material = iblockdata.getMaterial();

				if (fluid.matches(FluidTags.WATER) && (!(iblockdata.getBlock() instanceof FluidDrainable) || ((FluidDrainable) iblockdata.getBlock()).tryDrainFluid(this.stateLists.peek(), blockposition2, iblockdata) == Fluids.EMPTY) && !(iblockdata.getBlock() instanceof FluidBlock) && (material == Material.UNDERWATER_PLANT || material == Material.SEAGRASS)) {
					BlockEntity blockEntity = iblockdata.getBlock().hasBlockEntity() ? world.getBlockEntity(blockposition2) : null;
					dropStacks(iblockdata, world, blockposition2, blockEntity);
				}
				world.setBlockState(blockposition2, block.getHandle(), block.getFlag());
			}
		}
	}

	@Inject (method = "absorbWater", at = @At ("RETURN"))
	private void fukkit_cleanQueue(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		this.stateLists.remove();
	}
}

