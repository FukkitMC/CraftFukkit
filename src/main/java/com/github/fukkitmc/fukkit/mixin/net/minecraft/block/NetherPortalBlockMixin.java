package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.NetherPortalBlock$AreaHelperAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityTypeAccess;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
	@Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;spawn(Lnet/minecraft/world/World;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/text/Text;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnType;ZZ)Lnet/minecraft/entity/Entity;"))
	private Entity fukkit_netherPortalSpawnReason(EntityType type, World world, CompoundTag itemTag, Text name, PlayerEntity player, BlockPos pos, SpawnType spawnType, boolean alignPosition, boolean invertY) {
		return ((EntityTypeAccess)type).spawnCreature(world, itemTag, name, player, pos, spawnType, alignPosition, invertY, CreatureSpawnEvent.SpawnReason.NETHER_PORTAL);
	}

	private boolean created;
	@Redirect (method = "createPortalAt", at = @At (value = "INVOKE", target = "Lnet/minecraft/block/NetherPortalBlock$AreaHelper;createPortal()V"))
	private void fukkit_void(NetherPortalBlock.AreaHelper helper) {
		this.created = ((NetherPortalBlock$AreaHelperAccess)helper).createPortal();
	}

	@Inject(method = "createPortalAt", at = @At(value = "RETURN", ordinal = 0))
	private void fukkit_createPortalAt(IWorld world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(this.created);
	}
}
