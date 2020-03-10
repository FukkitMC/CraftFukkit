package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.world;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ThreadedAnvilChunkStorageAccess;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.*;

@Implements (@Interface (iface = ThreadedAnvilChunkStorageAccess.class, prefix = "fukkit$"))
@Mixin (ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {

	@Shadow @Final private WorldGenerationProgressListener worldGenerationProgressListener;

	public WorldGenerationProgressListener fukkit$getListener() {
		return this.worldGenerationProgressListener;
	}
	// todo finish
}
