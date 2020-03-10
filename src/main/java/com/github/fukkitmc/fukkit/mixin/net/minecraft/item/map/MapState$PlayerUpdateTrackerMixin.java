package com.github.fukkitmc.fukkit.mixin.net.minecraft.item.map;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.item.map.MapStateAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.network.Packet;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.map.RenderData;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.map.MapCursor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Mixin (MapState.PlayerUpdateTracker.class)
public class MapState$PlayerUpdateTrackerMixin {
	@Shadow @Final public PlayerEntity player;
	// todo yes
	@SuppressWarnings ("ShadowTarget")
	@Shadow private /*synthetic (this)*/ MapState field_132;
	private Collection<MapIcon> icons;

	@Inject (method = "getPacket", at = @At ("HEAD"))
	private void fukkit_renderData(ItemStack stack, CallbackInfoReturnable<Packet<?>> cir) {
		RenderData render = ((MapStateAccess) this.field_132).getMapView()
		                                                     .render((CraftPlayer) ((PlayerEntityAccess) this.player)
		                                                                           .getBukkit());
		this.icons = new ArrayList<>();
		for (MapCursor cursor : render.cursors) {
			if (cursor.isVisible()) {
				this.icons.add(new MapIcon(MapIcon.Type.byId(cursor.getRawType()), cursor.getX(), cursor.getY(), cursor
				                                                                                                 .getDirection(), CraftChatMessage
				                                                                                                                  .fromStringOrNull(cursor
				                                                                                                                                    .getCaption())));
			}
		}
	}

	@Redirect (method = "getPacket",
	           at = @At (value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<?> fukkit_icons(Map<?, ?> map) {
		Collection<MapIcon> temp = this.icons;
		this.icons = null;
		return temp;
	}
}
