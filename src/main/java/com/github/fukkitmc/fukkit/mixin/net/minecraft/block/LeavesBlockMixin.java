package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.event.block.LeavesDecayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {
	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/LeavesBlock;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"), cancellable = true)
	private void fukkit_decayEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		LeavesDecayEvent event = new LeavesDecayEvent(((WorldAccess)world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
		((WorldAccess)world).getBukkitServer().getPluginManager().callEvent(event);
		if(event.isCancelled() || world.getBlockState(pos).getBlock() != (Object)this) {
			ci.cancel();
		}
	}
}
