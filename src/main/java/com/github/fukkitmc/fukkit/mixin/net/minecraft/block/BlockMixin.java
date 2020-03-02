package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.BlockAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;

@Implements(@Interface(iface = BlockAccess.class, prefix = "fukkit$"))
@Mixin(Block.class)
public class BlockMixin {
	public int fukkit$getExpDrop(BlockState state, World world, BlockPos pos, ItemStack stack) {
		return 0;
	}

	@Redirect(method = "dropStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private static boolean fukkit_capture(World world, Entity entity) {
		List<ItemEntity> access = ((WorldAccess)world).getCaptureDrops();
		if(access != null && entity instanceof ItemEntity) // extra check
			access.add((ItemEntity) entity);
		else
			world.spawnEntity(entity);
		return false;
	}
}
