package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.Iterator;
import java.util.List;

@Mixin (BeehiveBlockEntity.class)
public abstract class BeehiveBlockEntityMixin extends BlockEntity {
	@Shadow
	protected abstract boolean releaseBee(BlockState state, CompoundTag compoundTag, List<Entity> list, BeehiveBlockEntity.BeeState beeState);

	public BeehiveBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject (method = "releaseBee",
	         at = @At (value = "JUMP",
	                   ordinal = 0),
	         slice = @Slice (from = @At (value = "OPCODE",
	                                     opcode = Opcodes.INSTANCEOF,
	                                     args = "desc=net/minecraft/entity/passive/BeeEntity")),
	         cancellable = true,
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_spawnReason(BlockState state, CompoundTag compoundTag, List<Entity> list, BeehiveBlockEntity.BeeState beeState, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, Direction direction, boolean bl, Entity entity) {
		if (!((ServerWorldAccess) this.world).addEntity(entity, CreatureSpawnEvent.SpawnReason.BEEHIVE)) cir.setReturnValue(false);
	}

	@Redirect (method = "releaseBee",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fukkit_movedUp(World world, Entity entity) {
		return true;
	}

	private Object bee;

	@Inject (method = "tickBees",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/block/entity/BeehiveBlockEntity;releaseBee(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundTag;Ljava/util/List;Lnet/minecraft/block/entity/BeehiveBlockEntity$BeeState;)Z"),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_ticks(CallbackInfo ci, Iterator iterator, BlockState blockState,
	                          @Coerce
	                          Object bee) {
		this.bee = bee;
	}

	@Redirect (method = "tickBees",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/block/entity/BeehiveBlockEntity;releaseBee(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundTag;Ljava/util/List;Lnet/minecraft/block/entity/BeehiveBlockEntity$BeeState;)Z"))
	private boolean fukkit_remove(BeehiveBlockEntity entity, BlockState state, CompoundTag compoundTag, List<Entity> list, BeehiveBlockEntity.BeeState beeState) {
		if (this.releaseBee(state, compoundTag, list, beeState)) {
			return true;
		} else {
			BeehiveBlockEntity$BeeAccess bee = (BeehiveBlockEntity$BeeAccess) this.bee;
			bee.setTicksInHive(bee.getMinOccupationTicks() / 2);
			return false;
		}
	}
}
