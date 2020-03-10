package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin (DetectorRailBlock.class)
public class DetectorRailBlockMixin {
	@ModifyVariable (method = "updatePoweredStatus", at = @At (value = "JUMP", ordinal = 1), index = 5, ordinal = 1)
	private boolean fukkit_redstoneEvent(boolean bl2, World world, BlockPos pos, BlockState state) {
		boolean powered = state.get(DetectorRailBlock.POWERED); // unfortunate duplicate call
		if (powered ^ bl2) {
			Block bukkit = ((WorldAccess) world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
			BlockRedstoneEvent event = new BlockRedstoneEvent(bukkit, powered ? 15 : 0, bl2 ? 15 : 0);
			((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
			return event.getNewCurrent() > 0;
		}
		return bl2;
	}
}
