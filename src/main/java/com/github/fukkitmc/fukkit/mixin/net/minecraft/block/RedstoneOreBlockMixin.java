package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.BlockAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Implements (@Interface (iface = BlockAccess.class, prefix = "fukkit$"))
@Mixin (RedstoneOreBlock.class)
public abstract class RedstoneOreBlockMixin extends Block {
	public RedstoneOreBlockMixin(Settings settings) {
		super(settings);
	}

	@Shadow
	private native static void spawnParticles(World world, BlockPos pos);

	@Redirect (method = "onBlockBreakStart", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/RedstoneOreBlock;light(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
	private void fukkit_interact(BlockState state, World world, BlockPos pos, BlockState state2, World world2, BlockPos pos2, PlayerEntity player) {
		interact(state, world, pos, player);
	}

	// this isn't verbatim craftbukkit, but in theory it should have the exact same effect
	@Inject (method = "onSteppedOn", at = @At ("HEAD"), cancellable = true)
	private void fukkit_addEntity(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		boolean isCancelled;
		if (entity instanceof ServerPlayerEntity) {
			PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent((PlayerEntity) entity, Action.PHYSICAL, pos, null, null, null);
			isCancelled = event.isCancelled();
		} else {
			EntityInteractEvent event = new EntityInteractEvent(((EntityAccess<?>) entity).getBukkit(), ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
			((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
			isCancelled = event.isCancelled();
		}
		if (isCancelled) ci.cancel();
	}

	@Redirect (method = "onSteppedOn", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/RedstoneOreBlock;light(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
	private void fukkit_interact(BlockState state, World world, BlockPos pos, World world2, BlockPos pos2, Entity entity) {
		interact(state, world, pos, entity);
	}

	@Redirect (method = "onUse", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/RedstoneOreBlock;light(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
	private void fukkit_interact(BlockState state, World world, BlockPos pos, BlockState state2, World world2, BlockPos pos2, PlayerEntity player, Hand hand, BlockHitResult hit) {
		interact(state, world, pos, player);
	}

	@Inject (method = "scheduledTick", at = @At (value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), cancellable = true)
	private void fukkit_fadeEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if (CraftEventFactory.callBlockFadeEvent(world, pos, state.with(RedstoneOreBlock.LIT, false)).isCancelled()) {
			ci.cancel();
		}
	}

	/**
	 * @author HalfOf2
	 * @reason handled by getExpDrop
	 */
	@Override
	@Overwrite
	public void onStacksDropped(BlockState state, World world, BlockPos pos, ItemStack stack) {
		super.onStacksDropped(state, world, pos, stack);
	}

	public int fukkit$getExpDrop(BlockState state, World world, BlockPos pos, ItemStack stack) {
		if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0) {
			return 1 + world.random.nextInt(5);
		}
		return 0;
	}

	private static void interact(BlockState state, World world, BlockPos pos, Entity entity) { // CraftBukkit - add Entity
		spawnParticles(world, pos);
		if (!state.get(RedstoneOreBlock.LIT)) {
			// CraftBukkit start
			if (CraftEventFactory.callEntityChangeBlockEvent(entity, pos, state.with(RedstoneOreBlock.LIT, true)).isCancelled()) {
				return;
			}
			// CraftBukkit end
			world.setBlockState(pos, state.with(RedstoneOreBlock.LIT, true), 3);
		}

	}
}
