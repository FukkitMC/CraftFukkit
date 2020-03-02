package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.function.Consumer;

@Mixin(BedBlock.class)
public class BedBlockMixin {
	@Redirect(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;canPlayersSleep()Z"))
	public boolean fukkit_forceTrue(Dimension dimension) {
		return true;
	}

	@Redirect(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBiome(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;"))
	private Biome fukkit_forceTrue(World world, BlockPos pos) {
		return Biomes.DEFAULT; // ocean with extra steps :tiny_potato:
	}

	@Redirect(method = "onUse", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;ifLeft(Ljava/util/function/Consumer;)Lcom/mojang/datafixers/util/Either;"))
	private Either<PlayerEntity.SleepFailureReason, Unit> fukkit_explodeBed(Either<PlayerEntity.SleepFailureReason, Unit> either, Consumer<PlayerEntity.SleepFailureReason> consumer, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		either.ifLeft(s -> {
			world.removeBlock(pos, false);
			BlockPos blockPos = pos.offset(state.get(HorizontalFacingBlock.FACING).getOpposite());
			if (world.getBlockState(blockPos).getBlock() == (Object)this) {
				world.removeBlock(blockPos, false);
			}
			world.createExplosion(null, DamageSource.netherBed(), (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, 5.0F, true, Explosion.DestructionType.DESTROY);
		});
		return either;
	}

}
