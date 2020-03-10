package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (CactusBlock.class)
public class CactusBlockMixin {
	@Redirect (method = "scheduledTick", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
	                                                        "(Lnet/minecraft/util/math/BlockPos;" +
	                                                        "Lnet/minecraft/block/BlockState;)Z",
	                                               ordinal = 0))
	private boolean fukkit_handleGrow(ServerWorld world, BlockPos pos, BlockState blockState) {
		return CraftEventFactory.handleBlockGrowEvent(world, pos, blockState);
	}

	@Inject (method = "onEntityCollision", at = @At ("HEAD"))
	private void fukkit_blockDamage(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		CraftEventFactory.blockDamage = ((WorldAccess) world).getBukkit()
		                                                     .getBlockAt(pos.getX(), pos.getZ(), pos.getY());
	}

	@Inject (method = "onEntityCollision", at = @At ("TAIL"))
	private void fukkit_blockDamageReset(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		CraftEventFactory.blockDamage = null; // seriously bukkit? static vars instead of parameters? are you kidding me?
		// atleast it's easy to implement in mixin
	}
}
