package com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.ai.brain.task;

import com.sun.org.apache.bcel.internal.generic.ILOAD;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.task.FarmerVillagerTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin (FarmerVillagerTask.class)
public class FarmerVillagerTaskMixin {
	@Shadow @Nullable private BlockPos currentTarget;

	@Redirect (method = "keepRunning",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/server/world/ServerWorld;breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_callChange(ServerWorld world, BlockPos pos, boolean drop, Entity breakingEntity) {
		if (!org.bukkit.craftbukkit.event.CraftEventFactory.callEntityChangeBlockEvent(breakingEntity, this.currentTarget, Blocks.AIR.getDefaultState()).isCancelled()) {
			world.breakBlock(this.currentTarget, true, breakingEntity);
		}
		return false;
	}

	// confirmed safe, setBlockstate is not threadsafe
	private static Block planted;

	@Redirect (method = "keepRunning",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean fukkit_getPlanted(ServerWorld world, BlockPos pos, BlockState state, int flags) {
		planted = state.getBlock();
		return false;
	}

	@ModifyVariable (method = "keepRunning",
	                 at = @At (value = "LOAD", opcode = Opcodes.ILOAD),
	                 index = 11,
	                 ordinal = 0,
	                 slice = @Slice (from = @At (value = "FIELD",
	                                             target = "Lnet/minecraft/block/Blocks;BEETROOTS:Lnet/minecraft/block/Block;")))
	private boolean fukkit_modify(boolean bl, ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
		if(planted != null && !CraftEventFactory.callEntityChangeBlockEvent(villagerEntity, this.currentTarget, planted.getDefaultState()).isCancelled()) {
			serverWorld.setBlockState(this.currentTarget, planted.getDefaultState(), 3);
			planted = null;
			return bl;
		}
		return false;
	}
}
