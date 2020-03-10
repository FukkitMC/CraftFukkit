package com.github.fukkitmc.fukkit.access.net.minecraft.world;

import net.minecraft.nbt.CompoundTag;
import java.io.File;
import java.util.UUID;

public interface WorldSaveHandlerAccess {
	CompoundTag getPlayerData(String uuid);

	UUID getUUID();

	File getPlayerDir();
}
