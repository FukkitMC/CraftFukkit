package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.minecraft.item.Items.*;

@Mixin (CauldronBlock.class)
public class CauldronBlockMixin extends Block {
	public CauldronBlockMixin(Settings settings) {
		super(settings);
	}

	@Redirect (method = "onEntityCollision", at = @At (value = "INVOKE",
	                                                   target = "Lnet/minecraft/block/CauldronBlock;setLevel" +
	                                                            "(Lnet/minecraft/world/World;" +
	                                                            "Lnet/minecraft/util/math/BlockPos;" +
	                                                            "Lnet/minecraft/block/BlockState;I)V"))
	private void fukkit_voidCall(CauldronBlock block, World world, BlockPos pos, BlockState state, int level) {}

	@Inject (method = "onEntityCollision",
	         at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;extinguish()V"),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_extinguishEntity(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci,
	                                     int i, float f) {
		if (!this.setLevel(world, pos, state, i - 1, entity, CauldronLevelChangeEvent.ChangeReason.EXTINGUISH)) {
			ci.cancel();
		}
	}

	private boolean setLevel(World world, BlockPos pos, BlockState state, int level, Entity entity,
	                         CauldronLevelChangeEvent.ChangeReason reason) {
		int newLevel = MathHelper.clamp(level, 0, 3);
		CauldronLevelChangeEvent event = new CauldronLevelChangeEvent(((WorldAccess) world).getBukkit()
		                                                                                   .getBlockAt(pos.getX(), pos
		                                                                                                           .getY(), pos
		                                                                                                                    .getZ()),
		(entity == null) ? null : ((EntityAccess<?>) entity).getBukkit(), reason, state
		                                                                          .get(CauldronBlock.LEVEL), newLevel);
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}
		world.setBlockState(pos, state.with(CauldronBlock.LEVEL, event.getNewLevel()), 2);
		world.updateHorizontalAdjacent(pos, this);
		return true;
	}

	@Inject (method = "onUse", at = @At (value = "INVOKE",
	                                     target = "Lnet/minecraft/item/DyeableItem;removeColor" +
	                                              "(Lnet/minecraft/item/ItemStack;)V"),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_armorWash(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
	                              BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir, ItemStack itemStack,
	                              int level, Item item) {
		if (!this.setLevel(world, pos, state, level - 1, player, CauldronLevelChangeEvent.ChangeReason.ARMOR_WASH)) {
			cir.setReturnValue(ActionResult.CONSUME);
		}
	}

	// would be faster to split this up into multiple mixins, but it's cleaner this way so meh
	@Inject (method = "onUse",
	         at = @At (value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;creativeMode:Z"),
	         locals = LocalCapture.CAPTURE_FAILHARD) // killing 7 birds with 1 stone
	private void fukkit_changeLevel2(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
	                                 BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir, ItemStack itemStack
	, int level, Item item) {
		CauldronLevelChangeEvent.ChangeReason reason = CauldronLevelChangeEvent.ChangeReason.UNKNOWN;
		if (item == WATER_BUCKET) {
			level = 3;
			reason = CauldronLevelChangeEvent.ChangeReason.BUCKET_EMPTY;
		} else if (item == BUCKET) {
			level = 0;
			reason = CauldronLevelChangeEvent.ChangeReason.BUCKET_FILL;
		} else if (item == GLASS_BOTTLE) {
			level--;
			reason = CauldronLevelChangeEvent.ChangeReason.BOTTLE_FILL;
		} else if (item == POTION && PotionUtil.getPotion(itemStack) == Potions.WATER) {
			level++;
			reason = CauldronLevelChangeEvent.ChangeReason.BOTTLE_EMPTY;
		} else if (level > 0 && item instanceof BannerItem) {
			level--;
			reason = CauldronLevelChangeEvent.ChangeReason.BANNER_WASH;
		}
		if (!this.setLevel(world, pos, state, level, player, reason)) { cir.setReturnValue(ActionResult.CONSUME); }
	}

	// handled by bukkit, killing 7 birds with one stone
	@Redirect (method = "onUse", at = @At (value = "INVOKE",
	                                       target = "Lnet/minecraft/block/CauldronBlock;setLevel" +
	                                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
	                                                "Lnet/minecraft/block/BlockState;I)V"))
	private void fukkit_voidAll(CauldronBlock block, World world, BlockPos pos, BlockState state, int level) { }

	/**
	 * @author HalfOf2
	 * @reason functionality is totally replaced with delegation call, could just be copy pasted twice but meh
	 */
	@Overwrite
	public void setLevel(World world, BlockPos pos, BlockState state, int level) {
		this.setLevel(world, pos, state, level, null, CauldronLevelChangeEvent.ChangeReason.UNKNOWN);
	}
}
