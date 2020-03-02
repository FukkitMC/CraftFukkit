package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LilyPadBlock.class)
public class LilyPadBlockMixin {
	@Redirect(method = "onEntityCollision", at = @At(value = "CONSTANT", args = "classValue=net/minecraft/entity/vehicle/BoatEntity"))
	private boolean fukkit_changeBlock(Object obj, Class type, BlockState state, World world, BlockPos pos, Entity entity) {
		return obj instanceof BoatEntity && !CraftEventFactory.callEntityChangeBlockEvent(entity, pos, Blocks.AIR.getDefaultState()).isCancelled();
	}
}
