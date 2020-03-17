package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockCookEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin (CampfireBlockEntity.class)
public class CampfireBlockEntityMixin extends BlockEntity {
	public CampfireBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject (method = "updateItemsBeingCooked",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"),
	         locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void fukkit_blockCookEvent(CallbackInfo ci, int i, ItemStack itemstack, Inventory inventory, ItemStack itemstack1, BlockPos blockPos) {
		CraftItemStack source = CraftItemStack.asCraftMirror(itemstack);
		org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemstack1);

		BlockCookEvent blockCookEvent = new BlockCookEvent(CraftBlock.at(this.world, this.pos), source, result);
		((WorldAccess)this.world).getBukkitServer().getPluginManager().callEvent(blockCookEvent);

		if (blockCookEvent.isCancelled()) {
			ci.cancel();
		}

		result = blockCookEvent.getResult();
		itemstack1 = CraftItemStack.asNMSCopy(result);
	}
}
