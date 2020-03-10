package com.github.fukkitmc.fukkit.mixin.net.minecraft.fluid;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.BaseFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockFromToEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Iterator;
import java.util.Map;

@Mixin (BaseFluid.class)
public abstract class BaseFluidMixin {
	@Inject (method = "tryFlow", at = @At (value = "INVOKE",
	                                       target = "Lnet/minecraft/fluid/BaseFluid;flow(Lnet/minecraft/world/IWorld;" +
	                                                "Lnet/minecraft/util/math/BlockPos;" +
	                                                "Lnet/minecraft/block/BlockState;" +
	                                                "Lnet/minecraft/util/math/Direction;" +
	                                                "Lnet/minecraft/fluid/FluidState;)V"),
	         cancellable = true)
	public void blockFromToEvent(IWorld world, BlockPos fluidPos, FluidState state, CallbackInfo ci) {
		org.bukkit.block.Block source = CraftBlock.at(world, fluidPos);
		BlockFromToEvent event = new BlockFromToEvent(source, BlockFace.DOWN);
		((WorldAccess) world.getWorld()).getBukkitServer().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject (
	method = "method_15744(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;" +
	         "Lnet/minecraft/fluid/FluidState;Lnet/minecraft/block/BlockState;)V",
	at = @At (value = "INVOKE",
	          target = "Lnet/minecraft/fluid/BaseFluid;flow(Lnet/minecraft/world/IWorld;" +
	                   "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;" +
	                   "Lnet/minecraft/util/math/Direction;Lnet/minecraft/fluid/FluidState;)V"),
	locals = LocalCapture.CAPTURE_FAILHARD)
	public void fukkit_fromToEvent(IWorld iWorld, BlockPos blockPos, FluidState fluidState, BlockState blockState,
	                               CallbackInfo ci, Map map, Iterator var7, Map.Entry entry, Direction direction,
	                               FluidState fluidState2, BlockPos blockPos2, BlockState blockState2) {
		org.bukkit.block.Block source = CraftBlock.at(iWorld, blockPos);
		BlockFromToEvent event = new BlockFromToEvent(source, CraftBlock.notchToBlockFace(direction));
		((WorldAccess) iWorld.getWorld()).getBukkitServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.flow(iWorld, blockPos2, blockState2, direction, fluidState2);
		}
	}

	@Shadow
	protected abstract void flow(IWorld world, BlockPos pos, BlockState state, Direction direction,
	                             FluidState fluidState);

	@Redirect (
	method = "method_15744(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;" +
	         "Lnet/minecraft/fluid/FluidState;Lnet/minecraft/block/BlockState;)V",
	at = @At (value = "INVOKE",
	          target = "Lnet/minecraft/fluid/BaseFluid;flow(Lnet/minecraft/world/IWorld;" +
	                   "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;" +
	                   "Lnet/minecraft/util/math/Direction;Lnet/minecraft/fluid/FluidState;)V"))
	public void fukkit_voidUnmapped(BaseFluid fluid, IWorld world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {

	}
}
