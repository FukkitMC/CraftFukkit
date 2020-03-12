package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;


import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.BasicInventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.bukkit.craftbukkit.inventory.CraftBlockInventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (targets = "net/minecraft/block/ComposterBlock$ComposterInventory")
public class ComposterBlock$ComposterInventoryMixin {
	@Inject (method = "<init>", at = @At ("TAIL"))
	private void fukkit_bukkitInventory(BlockState state, IWorld world, BlockPos pos, CallbackInfo ci) {
		((BasicInventoryAccess) this).setOwner(new CraftBlockInventoryHolder(world, pos, (Inventory) this));
	}
}
