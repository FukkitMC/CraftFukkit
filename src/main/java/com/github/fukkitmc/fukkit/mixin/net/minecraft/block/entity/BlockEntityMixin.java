package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.entity.BlockEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Implements(@Interface(iface = BlockEntityAccess.class, prefix = "fukkit$"))
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
	@Shadow protected World world;
	@Shadow protected BlockPos pos;
	private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
	public CraftPersistentDataContainer persistentDataContainer;

	@Inject(method = "fromTag", at = @At("RETURN"))
	private void fukkit_persistentData(CompoundTag tag, CallbackInfo ci) {
		this.persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);
		CompoundTag bukkitValues = tag.getCompound("PublicBukkitValues");
		if(bukkitValues != null)
			this.persistentDataContainer.putAll(bukkitValues);
	}

	@Inject(method = "toTag", at = @At("RETURN"))
	private void fukkit_persistentData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if(this.persistentDataContainer != null && !this.persistentDataContainer.isEmpty())
			tag.put("PublicBukkitValues", this.persistentDataContainer.toTagCompound());
	}


	public InventoryHolder fukkit$getOwner() {
		if(this.world == null)
			return null;
		BlockState state = ((WorldAccess) this.world).getBukkit().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()).getState();
		return state instanceof InventoryHolder ? (InventoryHolder) state : null;
	}

	public CraftPersistentDataContainer fukkit$getContainer() {
		return this.persistentDataContainer;
	}
	public void fukkit$setContainer(CraftPersistentDataContainer container) {
		this.persistentDataContainer = container;
	}
	public void fukkit$setWorld(World world) {
		this.world = world;
	}
}
