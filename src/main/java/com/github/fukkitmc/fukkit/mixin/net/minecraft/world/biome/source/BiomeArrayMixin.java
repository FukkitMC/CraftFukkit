package com.github.fukkitmc.fukkit.mixin.net.minecraft.world.biome.source;

import com.github.fukkitmc.fukkit.access.net.minecraft.world.biome.source.BiomeArrayAccess;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import org.spongepowered.asm.mixin.*;

import static net.minecraft.world.biome.source.BiomeArray.HORIZONTAL_BIT_MASK;
import static net.minecraft.world.biome.source.BiomeArray.VERTICAL_BIT_MASK;

@Implements (@Interface (iface = BiomeArrayAccess.class, prefix = "fukkit$"))
@Mixin (BiomeArray.class)
public class BiomeArrayMixin {
	@Shadow @Final private static int HORIZONTAL_SECTION_COUNT;
	@Shadow @Final private Biome[] data;

	@Unique
	public void fukkit$setBiome(int x, int y, int z, Biome biome) {
		int i = x & HORIZONTAL_BIT_MASK;
		int j = MathHelper.clamp(y, 0, VERTICAL_BIT_MASK);
		int k = z & HORIZONTAL_BIT_MASK;
		this.data[j << HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT | k << HORIZONTAL_SECTION_COUNT | i] =
		biome;
	}
}
