package com.github.fukkitmc.fukkit.mixin.net.minecraft.world;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.BlockViewAccess;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RayTraceContext;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Implements(@Interface(iface = BlockViewAccess.class, prefix = "fukkit$"))
@Mixin(BlockView.class)
public interface BlockViewMixin {
	@Shadow BlockState getBlockState(BlockPos pos);

	@Shadow FluidState getFluidState(BlockPos pos);

	@Shadow BlockHitResult rayTraceBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state);

	default BlockHitResult fukkit$rayTraceBlock(RayTraceContext raytrace1, BlockPos blockposition) {
		BlockState iblockdata = this.getBlockState(blockposition);
		FluidState fluid = this.getFluidState(blockposition);
		Vec3d vec3d = raytrace1.getStart();
		Vec3d vec3d1 = raytrace1.getEnd();
		VoxelShape voxelshape = raytrace1.getBlockShape(iblockdata, (BlockView) this, blockposition);
		BlockHitResult movingobjectpositionblock = this.rayTraceBlock(vec3d, vec3d1, blockposition, voxelshape, iblockdata);
		VoxelShape voxelshape1 = raytrace1.getFluidShape(fluid, (BlockView) this, blockposition);
		BlockHitResult movingobjectpositionblock1 = voxelshape1.rayTrace(vec3d, vec3d1, blockposition);
		double d0 = movingobjectpositionblock == null ? Double.MAX_VALUE : raytrace1.getStart().squaredDistanceTo(movingobjectpositionblock.getPos());
		double d1 = movingobjectpositionblock1 == null ? Double.MAX_VALUE : raytrace1.getStart().squaredDistanceTo(movingobjectpositionblock1.getPos());
		return d0 <= d1 ? movingobjectpositionblock : movingobjectpositionblock1;
	}
}
