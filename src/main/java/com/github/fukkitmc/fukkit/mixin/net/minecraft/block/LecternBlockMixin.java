package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LecternBlock.class)
public class LecternBlockMixin {
	@Redirect(method = "dropBook", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;"))
	private BlockEntity fukkit_dontValidate(World world, BlockPos pos) {
		return ((WorldAccess)world).getBlockEntity(pos,false);
	}

	@Inject(method = "dropBook", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;getOffsetX()I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_5500(BlockState state, World world, BlockPos pos, CallbackInfo ci, BlockEntity entity, LecternBlockEntity lectern, Direction facing, ItemStack current) {
		if(current.isEmpty())
			ci.cancel();
	}
}
