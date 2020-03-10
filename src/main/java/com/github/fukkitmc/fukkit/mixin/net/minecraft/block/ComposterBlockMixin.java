package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.util.Constructors;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (ComposterBlock.class)
public class ComposterBlockMixin {
	@Coerce
	@Redirect (method = "getInventory",
	           at = @At (value = "NEW", target = "net/minecraft/block/ComposterBlock$DummyInventory"))
	private BasicInventory fukkit_newInvType(BlockState state, IWorld world, BlockPos pos) {
		return Constructors.newComposterBlock$DummyInventory(world, pos);
	}
}
