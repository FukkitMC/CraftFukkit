package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.sun.org.apache.bcel.internal.generic.IF_ICMPEQ;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
	private int i;
	@Inject(method = "updateLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_power(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<BlockState> cir, BlockState newState, int i) {
		this.i = i;
	}

	// not exact same as but close enough tbh
	@Redirect(method = "updateLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"))
	private <T extends Comparable<T>> Object fukkit_redstoneEvent(BlockState state, Property<Integer> property, T value, World world, BlockPos pos, BlockState state2) {
		BlockRedstoneEvent event = new BlockRedstoneEvent(((WorldAccess)world).getBukkit().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), this.i, (Integer) value);
		((WorldAccess)world).getBukkitServer().getPluginManager().callEvent(event);
		return state.with(property, event.getNewCurrent());
	}
}
