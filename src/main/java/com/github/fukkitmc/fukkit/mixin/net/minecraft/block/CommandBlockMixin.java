package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.Objects;

@Mixin (CommandBlock.class)
public class CommandBlockMixin {
	@ModifyVariable (method = "neighborUpdate", at = @At (value = "INVOKE",
	                                                      target = "Lnet/minecraft/block/entity" +
	                                                               "/CommandBlockBlockEntity;setPowered(Z)V"),
	                 index = 9, ordinal = 1)
	private boolean fukkit_redstoneEvent(boolean bl, BlockState state, World world, BlockPos pos, Block block,
	                                     BlockPos neighborPos, boolean moved) {
		org.bukkit.block.Block bukkit = ((WorldAccess) world).getBukkit()
		                                                     .getBlockAt(pos.getX(), pos.getY(), pos.getZ());
		// unfortunate redundant call
		boolean isPowered = ((CommandBlockBlockEntity) Objects.requireNonNull(world
		                                                                      .getBlockEntity(pos), "ohno, something went miserably wrong."))
		                    .isPowered();
		int old = bl ? 15 : 0;
		int current = isPowered ? 15 : 0;
		BlockRedstoneEvent event = new BlockRedstoneEvent(bukkit, old, current);
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);
		return event.getNewCurrent() > 0;
	}
}
