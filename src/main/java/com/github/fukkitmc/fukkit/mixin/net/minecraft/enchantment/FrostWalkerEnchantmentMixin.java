package com.github.fukkitmc.fukkit.mixin.net.minecraft.enchantment;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Iterator;

import static net.minecraft.block.Blocks.FROSTED_ICE;

@Mixin (FrostWalkerEnchantment.class)
public class FrostWalkerEnchantmentMixin {
	@Inject (method = "freezeWater", at = @At (value = "INVOKE",
	                                           target = "Lnet/minecraft/world/World;setBlockState" +
	                                                    "(Lnet/minecraft/util/math/BlockPos;" +
	                                                    "Lnet/minecraft/block/BlockState;)Z"),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private static void inject(LivingEntity entity, World world, BlockPos blockPos, int level, CallbackInfo ci,
	                           BlockState blockState, float f, BlockPos.Mutable mutable, Iterator var7,
	                           BlockPos blockPos2, BlockState blockState3) {
		if (CraftEventFactory.handleBlockFormEvent(world, blockPos2, blockState, entity)) {
			world.getBlockTickScheduler()
			     .schedule(blockPos2, FROSTED_ICE, MathHelper.nextInt(entity.getRandom(), 60, 120));
		}
	}

	@Redirect (method = "freezeWater", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/world/World;setBlockState" +
	                                                      "(Lnet/minecraft/util/math/BlockPos;" +
	                                                      "Lnet/minecraft/block/BlockState;)Z"))
	private static boolean setBlockState(World world, BlockPos pos, BlockState blockState) {
		return false;
	}

	@Redirect (method = "freezeWater", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/world/TickScheduler;schedule(Lnet/minecraft/util/math/BlockPos;Ljava/lang/Object;I)V"))
	private static void schedule(TickScheduler scheduler, BlockPos pos, Object object, int delay) {}
}
