package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockFormEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (ConcretePowderBlock.class)
public abstract class ConcretePowderBlockMixin extends FallingBlock {
	public ConcretePowderBlockMixin(Settings settings) {
		super(settings);
	}

	@Shadow
	private static native boolean method_24279(BlockView blockView, BlockPos blockPos, BlockState blockState);

	@Shadow
	@Final
	private BlockState hardenedState;

	@Shadow protected native static boolean hardensOnAnySide(BlockView view, BlockPos pos);

	@Redirect (method = "onLanding", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_handleFormEvent(World world, BlockPos pos, BlockState state, int flags) {
		return CraftEventFactory.handleBlockFormEvent(world, pos, state, flags);
	}


	@Inject (method = "getPlacementState", at = @At (value = "JUMP"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void fukkit_onPlace(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir, BlockView view, BlockPos pos, BlockState state) {
		if (!method_24279(view, pos, state)) cir.setReturnValue(super.getPlacementState(ctx));
		CraftBlockState bukkitState = CraftBlockState.getBlockState((IWorld) view, pos);
		bukkitState.setData(this.hardenedState);

		BlockFormEvent event = new BlockFormEvent(bukkitState.getBlock(), bukkitState);
		((WorldAccess) view).getBukkitServer().getPluginManager().callEvent(event); // technically this isn't exactly how it's implemented, but meh

		if (!event.isCancelled()) {
			cir.setReturnValue(bukkitState.getHandle());
		}

		cir.setReturnValue(super.getPlacementState(ctx));
	}

	/**
	 * @author HalfOf2
	 * @reason Method is replaced completely, could be replaced with a inject cancel but that doesn't really accomplish anything
	 */
	@Override
	@Overwrite
	public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
		if (hardensOnAnySide(world, pos)) {
			CraftBlockState blockState = CraftBlockState.getBlockState(world, pos);
			blockState.setData(this.hardenedState);

			BlockFormEvent event = new BlockFormEvent(blockState.getBlock(), blockState);
			((WorldAccess)world).getBukkitServer().getPluginManager().callEvent(event); // not exact

			if (!event.isCancelled()) {
				return blockState.getHandle();
			}
		}

		return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
	}
}
