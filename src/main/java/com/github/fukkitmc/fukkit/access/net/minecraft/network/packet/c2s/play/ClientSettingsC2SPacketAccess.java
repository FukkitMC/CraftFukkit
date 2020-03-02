package com.github.fukkitmc.fukkit.access.net.minecraft.network.packet.c2s.play;

import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// todo add to mixin package
@Mixin(ClientSettingsC2SPacket.class)
public interface ClientSettingsC2SPacketAccess {
	@Accessor("viewDistance") int getViewDistance();
}
