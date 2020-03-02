package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.CraftHandled;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
import java.util.Random;

@Mixin (AbstractButtonBlock.class)
public class AbstractButtonBlockMixin {
	@Shadow
	@Final
	public static BooleanProperty POWERED;

	@Inject (method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/AbstractButtonBlock;method_21845(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"), cancellable = true)
	public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
		boolean powered = state.get(POWERED);
		Block block = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		int old = powered ? 15 : 0;
		int current = powered ? 0 : 15;
		BlockRedstoneEvent event = new BlockRedstoneEvent(block, old, current);
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
		if (event.getNewCurrent() > 0 == powered) {
			cir.setReturnValue(ActionResult.SUCCESS);
		}
	}

	@Inject (method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), cancellable = true)
	public void schedule(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		Block block = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		BlockRedstoneEvent event = new BlockRedstoneEvent(block, 15, 0);
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
		if (event.getNewCurrent() > 0) ci.cancel();
	}


	@Inject (method = "tryPowerWithProjectiles(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At ("HEAD"), cancellable = true)
	public void entityHitButton(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		List<? extends Entity> list = world.getNonSpectatingEntities(ProjectileEntity.class, state.getOutlineShape(world, pos).getBoundingBox().offset(pos));
		boolean bl = !list.isEmpty();
		boolean bl2 = state.get(POWERED);
		if (bl != bl2 && bl) {
			Block block = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
			boolean allowed = false;
			for (Entity entity : list) {
				if (entity != null) {
					EntityInteractEvent event = new EntityInteractEvent(((CraftHandled<org.bukkit.entity.Entity>) entity).getBukkit(), block);
					((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						allowed = true;
						break;
					}
				}
			}

			if (!allowed) ci.cancel();
		}
	}

	@Inject (method = "tryPowerWithProjectiles", at = @At (value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	public void entityHitButton2(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		boolean powered = state.get(AbstractButtonBlock.POWERED);
		Block block = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		int old = (powered) ? 15 : 0;
		int current = (!powered) ? 15 : 0;

		BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, old, current);
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(eventRedstone);

		List<? extends Entity> list = world.getNonSpectatingEntities(ProjectileEntity.class, state.getOutlineShape(world, pos).getBoundingBox().offset(pos));
		boolean flag = !list.isEmpty();
		if ((flag && eventRedstone.getNewCurrent() <= 0) || (!flag && eventRedstone.getNewCurrent() > 0)) {
			ci.cancel();
		}
	}
}
