package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (TripwireHookBlock.class)
public class TripwireHookBlockMixin {
	@Inject (method = "update", at = @At (value = "INVOKE",
	                                      target = "Lnet/minecraft/block/TripwireHookBlock;playSound" +
	                                               "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
	                                               "ZZZZ)V",
	                                      ordinal = 1), cancellable = true)
	private void fukkit_redstoneEvent(World world, BlockPos pos, BlockState state, boolean beingRemoved, boolean bl,
	                                  int i, BlockState blockState, CallbackInfo ci) {
		Block block = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		BlockRedstoneEvent event = new BlockRedstoneEvent(block, 15, 0);
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
		if (event.getNewCurrent() > 0) { ci.cancel(); }
	}

}
