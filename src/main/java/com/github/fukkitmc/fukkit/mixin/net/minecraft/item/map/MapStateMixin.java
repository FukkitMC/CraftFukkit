package com.github.fukkitmc.fukkit.mixin.net.minecraft.item.map;

import com.github.fukkitmc.fukkit.access.net.minecraft.item.map.MapStateAccess;
import com.github.fukkitmc.fukkit.util.Constructors;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.dimension.DimensionType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.map.CraftMapView;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.UUID;

@Implements (@Interface (prefix = "fukkit$", iface = MapStateAccess.class))
@Mixin (MapState.class)
public class MapStateMixin {
	@Shadow public DimensionType dimension;
	@Shadow @Final public Map<String, MapIcon> icons;
	public CraftMapView mapView;
	private CraftServer server;
	private UUID worldUUID;

	@Inject (method = "<init>", at = @At ("TAIL"))
	private void fukkit_init(String key, CallbackInfo ci) {
		this.mapView = new CraftMapView((MapState) (Object) this);
		this.server = (CraftServer) Bukkit.getServer();
	}

	@Redirect (method = "fromTag", at = @At (value = "INVOKE",
	                                         target = "Lnet/minecraft/world/dimension/DimensionType;byRawId(I)" +
	                                                  "Lnet/minecraft/world/dimension/DimensionType;"))
	private DimensionType fukkit_corruption(int i, CompoundTag tag) {
		DimensionType type = null;
		if (i >= CraftWorld.CUSTOM_DIMENSION_OFFSET) {
			long least = tag.getLong("UUIDLeast");
			long most = tag.getLong("UUIDMost");

			if (least != 0 && most != 0) {
				this.worldUUID = new UUID(most, least);
				CraftWorld world = (CraftWorld) this.server.getWorld(this.worldUUID);
				if (world == null) {
					// todo verify
					type = Constructors.newDimensionType(127, null, null, null, false, null, DimensionType.OVERWORLD);
				} else {
					type = world.getHandle().getDimension().getType();
				}
			}
		} else {
			type = DimensionType.byRawId(i);
		}
		return type;
	}

	@Inject (method = "toTag", at = @At (value = "HEAD"))
	private void fukkit_corruption(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (this.dimension.getRawId() >= CraftWorld.CUSTOM_DIMENSION_OFFSET) {
			if (this.worldUUID == null) {
				for (World world : this.server.getWorlds()) {
					CraftWorld craft = (CraftWorld) world;
					if (craft.getHandle().getDimension().getType() == this.dimension) {
						this.worldUUID = craft.getUID();
						break;
					}
				}
			}

			if (this.worldUUID != null) {
				tag.putLong("UUIDLeast", this.worldUUID.getLeastSignificantBits());
				tag.putLong("UUIUDMost", this.worldUUID.getMostSignificantBits());
			}
		}
	}

	public CraftMapView fukkit$getMapView() {
		return this.mapView;
	}
}
