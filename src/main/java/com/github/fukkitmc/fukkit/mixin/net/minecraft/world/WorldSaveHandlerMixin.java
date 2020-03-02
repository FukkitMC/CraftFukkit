package com.github.fukkitmc.fukkit.mixin.net.minecraft.world;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldSaveHandlerAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.WorldSaveHandler;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.io.*;
import java.util.UUID;

@Implements (@Interface (iface = WorldSaveHandlerAccess.class, prefix = "fukkit$"))
@Mixin (WorldSaveHandler.class)
public class WorldSaveHandlerMixin {
	@Shadow
	@Final
	private File playerDataDir;
	@Shadow
	@Final
	private static final Logger LOGGER = null;
	@Shadow @Final private File worldDir;
	private UUID uuid;

	@Inject (method = "loadPlayerData", at = @At (value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z"))
	private void fukkit_updateOldPlayer(PlayerEntity playerEntity, CallbackInfoReturnable<CompoundTag> cir) {
		CraftPlayer player = (CraftPlayer) ((EntityAccess) playerEntity).getBukkit();
		long modified = new File(this.playerDataDir, playerEntity.getUuidAsString() + ".dat").lastModified();
		if (modified < player.getFirstPlayed()) player.setFirstPlayed(modified);
	}

	public CompoundTag fukkit$getPlayerData(String uuid) {
		try {
			File playerData = new File(this.playerDataDir, uuid + ".dat");
			if (playerData.exists()) {
				return NbtIo.readCompressed(new FileInputStream(playerData));
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to load player data for " + uuid);
		}
		return null;
	}

	public UUID fukkit$getUUID() {
		if (this.uuid != null) return this.uuid;
		File file1 = new File(this.worldDir, "uid.dat");
		if (file1.exists()) {
			DataInputStream dis = null;
			try {
				dis = new DataInputStream(new FileInputStream(file1));
				return this.uuid = new UUID(dis.readLong(), dis.readLong());
			} catch (IOException ex) {
				LOGGER.warn("Failed to read " + file1 + ", generating new random UUID", ex);
			} finally {
				if (dis != null) {
					try {
						dis.close();
					} catch (IOException ex) {
						// NOOP
					}
				}
			}
		}
		this.uuid = UUID.randomUUID();
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(file1));
			dos.writeLong(this.uuid.getMostSignificantBits());
			dos.writeLong(this.uuid.getLeastSignificantBits());
		} catch (IOException ex) {
			LOGGER.warn("Failed to write " + file1, ex);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException ex) {
					// NOOP
				}
			}
		}
		return this.uuid;
	}

	public File fukkit$getPlayerDir() {
		return this.playerDataDir;
	}
}
