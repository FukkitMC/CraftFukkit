package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.NetherPortalBlock$AreaHelperAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.event.world.PortalCreateEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Implements (@Interface (iface = NetherPortalBlock$AreaHelperAccess.class, prefix = "fukkit$"))
@Mixin (NetherPortalBlock.AreaHelper.class)
public class NetherPortalBlock$AreaHelperMixin {
	@Shadow
	private int width;
	@Shadow
	private BlockPos lowerCorner;
	@Shadow
	private int height;
	@Shadow
	@Final
	private IWorld world;
	@Shadow
	@Final
	private Direction negativeDir;
	@Shadow
	@Final
	private Direction.Axis axis;
	private List<BlockState> blocks = new ArrayList<>();

	@Inject (method = "findHeight", at = @At ("HEAD"))
	private void fukkit_clear(CallbackInfoReturnable<Integer> cir) {
		this.blocks.clear();
	}

	/**
	 * @author HalfOf2
	 * @reason md5pls
	 */
	@Overwrite
	public void createPortal() {
		throw new UnsupportedOperationException("Half you idiot you forgot to replace this with injects");
	}

	// 3 birds 1 stone
	@Redirect(method = "findHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/block/NetherPortalBlock$AreaHelper;validStateInsidePortal(Lnet/minecraft/block/BlockState;)Z")))
	private net.minecraft.block.BlockState fukkit_height(IWorld world, BlockPos pos) {
		net.minecraft.block.BlockState state = world.getBlockState(pos);
		if(state.getBlock() == Blocks.OBSIDIAN) {
			this.blocks.add(CraftBlock.at(world, pos).getState());
		}
		return state;
	}

	public boolean fukkit$createPortal() {
		org.bukkit.World bworld = ((WorldAccess) this.world.getWorld()).getBukkit();

		// Copy below for loop
		for (int i = 0; i < this.width; ++i) {
			BlockPos pos = this.lowerCorner.offset(this.negativeDir, i);

			for (int j = 0; j < this.height; ++j) {
				BlockPos up = pos.up(j);
				CraftBlockState state = CraftBlockState.getBlockState(this.world.getWorld(), up, 18);
				state.setData(Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, this.axis));
				this.blocks.add(state);
			}
		}

		PortalCreateEvent event = new PortalCreateEvent(this.blocks, bworld, null, PortalCreateEvent.CreateReason.FIRE);
		((WorldAccess) this.world.getWorld()).getBukkitServer().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			return false;
		}
		// CraftBukkit end
		for (int i = 0; i < this.width; ++i) {
			BlockPos blockposition = this.lowerCorner.offset(this.negativeDir, i);

			for (int j = 0; j < this.height; ++j) {
				this.world.setBlockState(blockposition.up(j), Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, this.axis), 18);
			}
		}

		return true; // CraftBukkit
	}
}
