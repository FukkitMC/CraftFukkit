package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.util.Constants;
import net.minecraft.block.DispenserBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (DispenserBlock.class)
public class DispenserBlockMixin {
	@Inject (method = "dispense", at = @At (value = "INVOKE",
	                                        target = "Lnet/minecraft/block/entity/DispenserBlockEntity;getInvStack(I)" +
	                                                 "Lnet/minecraft/item/ItemStack;"))
	private void fukkit_resetEventStatus(World world, BlockPos pos, CallbackInfo ci) {
		Constants.DISPENSER_EVENT_FIRED = false;
	}
}
