package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.BasicInventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.bukkit.craftbukkit.inventory.CraftBlockInventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (targets = "net/minecraft/block/ComposterBlock$ComposterInventory")
public abstract class ComposterBlock$FullComposterInventoryMixin extends BasicInventory implements SidedInventory {
	@Shadow private boolean dirty;

	@Shadow @Final private IWorld world;

	@Shadow @Final private BlockPos pos;

	@Shadow @Final private BlockState state;

	@Inject (method = "<init>", at = @At ("TAIL"))
	private void onInit(BlockState state, IWorld world, BlockPos pos, CallbackInfo ci) {
		((BasicInventoryAccess) this).setOwner(new CraftBlockInventoryHolder(world, pos, this));
	}

	@Inject (method = "markDirty", at = @At ("HEAD"), cancellable = true)
	private void fukkit_allowPutback(CallbackInfo ci) {
		if (!this.isInvEmpty()) {
			this.dirty = false;
			this.world.setBlockState(this.pos, this.state, 3);
			ci.cancel();
		}
	}
}
