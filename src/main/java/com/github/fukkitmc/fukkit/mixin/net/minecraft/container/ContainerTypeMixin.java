package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.util.Constructors;
import net.minecraft.container.ContainerType;
import net.minecraft.container.LecternContainer;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ContainerType.class)
public class ContainerTypeMixin {
	/**
	 * @author HalfOf2
	 */
	@SuppressWarnings ("OverwriteTarget")
	@Overwrite
	private static /*synthetic*/ LecternContainer method_17436(int i, PlayerInventory inventory) {
		return Constructors.newLecternContainer(i, inventory);
	}
}
