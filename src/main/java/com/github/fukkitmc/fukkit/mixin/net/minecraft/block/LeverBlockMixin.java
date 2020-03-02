package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (LeverBlock.class)
public class LeverBlockMixin {

	@Inject (method = "onUse", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/LeverBlock;method_21846(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), cancellable = true)
	private void fukkit_redstoneEvent(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
		boolean oldPower = state.get(LeverBlock.POWERED);
		Block block = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		int old = oldPower ? 15 : 0;
		int current = oldPower ? 0 : 15;

		BlockRedstoneEvent event = new BlockRedstoneEvent(block, old, current);
		((WorldAccess)world).getBukkitServer().getPluginManager().callEvent(event);

		if((event.getNewCurrent() > 0) == oldPower) {
			cir.setReturnValue(ActionResult.SUCCESS);
		}
	}
}
