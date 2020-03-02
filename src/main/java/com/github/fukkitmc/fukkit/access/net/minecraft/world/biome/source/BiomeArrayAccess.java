package com.github.fukkitmc.fukkit.access.net.minecraft.world.biome.source;

import net.minecraft.world.biome.Biome;

public interface BiomeArrayAccess {
	void setBiome(int x, int y, int z, Biome biome);
}
