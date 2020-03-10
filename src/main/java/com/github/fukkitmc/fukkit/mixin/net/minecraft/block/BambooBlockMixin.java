package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.devtech.utilib.functions.ThrowingSupplier;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.lang.reflect.Field;
import java.util.Random;

@Mixin (BambooBlock.class)
public class BambooBlockMixin extends Block {
	private static final Field RANDOM = ((ThrowingSupplier<Field>) () -> Random.class.getDeclaredField("seed")).get();

	@Shadow
	@Final
	public static IntProperty AGE;

	// hahayes local variables :crab: multithreading is gone :crab:
	// this is likely not a good idea and will most definetly break something
	// TODO fix
	private boolean shouldUpdateOthers;


	public BambooBlockMixin(Settings settings) {
		super(settings);
	}

	@Redirect (method = "updateLeaves", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/block/BlockState;getBlock()" +
	                                                       "Lnet/minecraft/block/Block;",
	                                              ordinal = 2))
	private Block fukkit_quickHack(BlockState state) {
		this.shouldUpdateOthers = state.getBlock() == Blocks.BAMBOO;

		return Blocks.STONE; // idfk just choose a block to make the if be false
	}

	@Inject (method = "updateLeaves", at = @At (value = "INVOKE",
	                                            target = "Lnet/minecraft/world/World;setBlockState" +
	                                                     "(Lnet/minecraft/util/math/BlockPos;" +
	                                                     "Lnet/minecraft/block/BlockState;I)Z",
	                                            ordinal = 2), cancellable = true,
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_spreadEvent(BlockState state, World world, BlockPos pos, Random random, int height,
	                                CallbackInfo ci, BlockState blockState, BlockPos blockPos, BlockState blockState2,
	                                BambooLeaves bambooLeaves, int i, int j) {
		if (org.bukkit.craftbukkit.event.CraftEventFactory
		    .handleBlockSpreadEvent(world, pos, pos.up(), this.getDefaultState().with(BambooBlock.AGE, j)
		                                                      .with(BambooBlock.LEAVES, bambooLeaves)
		                                                      .with(BambooBlock.STAGE, j), 3)) {
			if (this.shouldUpdateOthers) {
				world.setBlockState(pos.down(), blockState.with(BambooBlock.LEAVES, BambooLeaves.SMALL), 3);
				world.setBlockState(blockPos, blockState2.with(BambooBlock.LEAVES, BambooLeaves.NONE), 3);
			}
		}
		ci
		.cancel(); // prevent the other method from running, in theory we could just redirect it, but I'm lazy :tiny_potato:
	}
}
