package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.PluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractPressurePlateBlock.class)
public abstract class AbstractPressurePlateBlockMixin {
	@Shadow protected abstract int getRedstoneOutput(World world, BlockPos pos);

	@Redirect (method = "updatePlateState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractPressurePlateBlock;getRedstoneOutput(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", ordinal = 0))
	public int fukkit_blockRedstoneEvent(AbstractPressurePlateBlock block, World world, BlockPos pos, World world2, BlockPos pos2, BlockState blockState, int rsOut) {
		int i = this.getRedstoneOutput(world, pos);
		// this works, only in theory, I've yet to see it in practice
		// it's like communism :tiny_potato:
		org.bukkit.World bukkit = ((WorldAccess)world).getBukkit();
		PluginManager manager = ((WorldAccess)world).getBukkitServer().getPluginManager();

		if((rsOut > 0) ^ (i > 0)) {
			BlockRedstoneEvent event = new BlockRedstoneEvent(bukkit.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), rsOut, i);
			manager.callEvent(event);
			i = event.getNewCurrent();
		}
		return i;
	}
}
