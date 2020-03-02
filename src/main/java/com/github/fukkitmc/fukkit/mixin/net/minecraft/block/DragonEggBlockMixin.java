package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.util.math.Vec3iAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockFromToEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (DragonEggBlock.class)
public class DragonEggBlockMixin {
	@Inject (method = "teleport", at = @At (value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_fromToEvent(BlockState state, World world, BlockPos fromPos, CallbackInfo ci, int i, BlockPos toPos) {
		Block from = ((WorldAccess) world).getBukkit().getBlockAt(fromPos.getX(), fromPos.getY(), fromPos.getZ());
		Block to = ((WorldAccess) world).getBukkit().getBlockAt(toPos.getX(), toPos.getY(), toPos.getZ());
		BlockFromToEvent event = new BlockFromToEvent(from, to);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled())
			ci.cancel();
		else {
			// haha yes
			Vec3iAccess access = (Vec3iAccess) toPos;
			Block newTo = event.getToBlock();
			access.setX(newTo.getX());
			access.setY(newTo.getY());
			access.setZ(newTo.getZ());
		}
	}
}
