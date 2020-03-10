package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings ("ShadowTarget") @Mixin (targets = "net/minecraft/container/BlockContext$2")
public class BlockContext$2Mixin implements BlockContextMixin {
	@Shadow @Final /*synthetic*/ World field_17305;
	@Shadow @Final /*synthetic*/ BlockPos field_17306;

	@Override
	public World getWorld() {
		return this.field_17305;
	}

	@Override
	public BlockPos getPosition() {
		return this.field_17306;
	}

}
