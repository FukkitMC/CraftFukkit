package com.github.fukkitmc.fukkit.access.net.minecraft.server.world;

import net.minecraft.server.WorldGenerationProgressListener;

public interface ThreadedAnvilChunkStorageAccess {
	WorldGenerationProgressListener getListener();
}
