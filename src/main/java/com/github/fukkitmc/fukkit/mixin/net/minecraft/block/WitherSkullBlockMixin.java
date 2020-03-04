package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.LinkedList;
import java.util.Queue;

@Mixin(WitherSkullBlock.class)
public class WitherSkullBlockMixin {
	private static Queue<BlockStateListPopulator> populators = new LinkedList<>();
	@Inject(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V", at = @At("HEAD"), cancellable = true)
	private static void fukkit_captureStates(World world, BlockPos pos, SkullBlockEntity blockEntity, CallbackInfo ci) {
		if(((WorldAccess)world).capturesBlockStates())
			ci.cancel();
		else {
			populators.add(new BlockStateListPopulator(world));
		}
	}

	@Inject(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V", at = @At("RETURN"), cancellable = true)
	private static void fukkit_releaseQueue(World world, BlockPos pos, SkullBlockEntity blockEntity, CallbackInfo ci) {
		populators.poll(); // release queue
	}

	@Redirect(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V", at = @At(value = "INVOKE",target = "Lnet/minecraft/world/World;playLevelEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
	private static void fukkit_voidCall(World world, int eventId, BlockPos blockPos, int data) {}

	@Redirect(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V", at = @At(value = "INVOKE",target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private static boolean fukkit_populator(World world, BlockPos pos, BlockState state, int flags) {
		return populators.peek().setBlockState(pos, state, flags);
	}

	@Inject(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getNonSpectatingEntities(Ljava/lang/Class;Lnet/minecraft/util/math/Box;)Ljava/util/List;"), locals = LocalCapture.PRINT)
	private static void fukkit_updateList(World world, BlockPos pos, SkullBlockEntity blockEntity, CallbackInfo ci, BlockPattern blockPattern, BlockPattern.Result result, WitherEntity witherEntity) {
		if(!((ServerWorldAccess)world).addEntity(witherEntity, CreatureSpawnEvent.SpawnReason.BUILD_WITHER))
			ci.cancel();
		else {
			BlockStateListPopulator populator = populators.peek();
			for (BlockPos newPos : populator.getBlocks()) {
				world.playLevelEvent(2001, pos, Block.getRawIdFromState(world.getBlockState(newPos)));
			}
			populator.updateList();
		}
	}

	@Redirect(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private static boolean fukkit_handledUp(World world, Entity entity) {
		return false;
	}
}
