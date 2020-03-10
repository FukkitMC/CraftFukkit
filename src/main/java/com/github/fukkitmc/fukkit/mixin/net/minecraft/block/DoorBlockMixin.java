package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin (DoorBlock.class)
public abstract class DoorBlockMixin {
	/**
	 * @author HalfOf2
	 * @reason most of the logic is rewritten, it might be possible to do this without overwrite, but it's 10pm and
	 * the patch is big so meh
	 */
	@Overwrite
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos,
	                           boolean moved) {
		BlockPos otherHalf = pos.offset(
		state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);

		org.bukkit.World bukkitWorld = ((WorldAccess) world).getBukkit();
		org.bukkit.block.Block bukkitBlock = bukkitWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		org.bukkit.block.Block blockTop = bukkitWorld.getBlockAt(otherHalf.getX(), otherHalf.getY(), otherHalf.getZ());

		int power = bukkitBlock.getBlockPower();
		int powerTop = blockTop.getBlockPower();
		if (powerTop > power) { power = powerTop; }
		int oldPower = state.get(DoorBlock.POWERED) ? 15 : 0;

		if (oldPower == 0 ^ power == 0) {
			BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bukkitBlock, oldPower, power);
			((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(eventRedstone);

			boolean flag1 = eventRedstone.getNewCurrent() > 0;
			// CraftBukkit end
			if (flag1 != state.get(DoorBlock.OPEN)) {
				this.playOpenCloseSound(world, pos, flag1);
			}

			world.setBlockState(pos, state.with(DoorBlock.POWERED, flag1).with(DoorBlock.OPEN, flag1), 2);
		}
	}

	@Shadow
	protected abstract void playOpenCloseSound(World world, BlockPos pos, boolean open);
}
