package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.entity.BlockEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (BlockEntity.class)
public abstract class BlockEntityMixin implements BlockEntityAccess {
	private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
	public CraftPersistentDataContainer persistentDataContainer;
	@Shadow protected World world;
	@Shadow protected BlockPos pos;

	@Shadow public native net.minecraft.block.BlockState getCachedState();

	@Shadow @Nullable
	public native World getWorld();

	@Inject (method = "fromTag", at = @At ("RETURN"))
	private void fukkit_persistentData(CompoundTag tag, CallbackInfo ci) {
		this.persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);
		CompoundTag bukkitValues = tag.getCompound("PublicBukkitValues");
		if (bukkitValues != null) { this.persistentDataContainer.putAll(bukkitValues); }
	}

	@Inject (method = "toTag", at = @At ("RETURN"))
	private void fukkit_persistentData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (this.persistentDataContainer != null && !this.persistentDataContainer.isEmpty()) {
			tag.put("PublicBukkitValues", this.persistentDataContainer.toTagCompound());
		}
	}


	public InventoryHolder getOwner() {
		if (this.world == null) { return null; }
		BlockState state = ((WorldAccess) this.world).getBukkit()
		                                             .getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ())
		                                             .getState();
		return state instanceof InventoryHolder ? (InventoryHolder) state : null;
	}

	@Override
	public CraftPersistentDataContainer getContainer() {
		return this.persistentDataContainer;
	}

	@Override
	public void setContainer(CraftPersistentDataContainer container) {
		this.persistentDataContainer = container;
	}

	@Override
	public void setWorld(World world) {
		this.world = world;
	}
}
