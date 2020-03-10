package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin (TrapdoorBlock.class)
public class TrapdoorBlockMixin {
	@ModifyVariable (method = "neighborUpdate", at = @At (value = "INVOKE",
	                                                      target = "Lnet/minecraft/block/BlockState;get" +
	                                                               "(Lnet/minecraft/state/property/Property;)" +
	                                                               "Ljava/lang/Comparable;",
	                                                      ordinal = 0), index = 7, ordinal = 1, slice = @Slice (
	from = @At (value = "FIELD",
	            target = "Lnet/minecraft/block/TrapdoorBlock;OPEN:Lnet/minecraft/state/property/BooleanProperty;",
	            ordinal = 0)))
	private boolean fukkit_modVar(boolean var, BlockState state, World world, BlockPos pos, Block block,
	                              BlockPos neighborPos, boolean moved) {
		org.bukkit.World bworld = ((WorldAccess) world).getBukkit();
		org.bukkit.block.Block bblock = bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());

		int power = bblock.getBlockPower();
		int oldPower = state.get(TrapdoorBlock.OPEN) ? 15 : 0;

		if (oldPower == 0 ^ power == 0 || block.getDefaultState().emitsRedstonePower()) {
			BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bblock, oldPower, power);
			((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(eventRedstone);
			return eventRedstone.getNewCurrent() > 0;
		}
		return var;
	}
}
