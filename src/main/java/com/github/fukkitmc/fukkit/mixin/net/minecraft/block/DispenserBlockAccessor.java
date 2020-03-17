package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin (DispenserBlock.class)
public interface DispenserBlockAccessor {
	@Accessor
	static Map<Item, DispenserBehavior> getBEHAVIORS() { throw new UnsupportedOperationException(); }
}
