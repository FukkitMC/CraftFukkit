package com.github.fukkitmc.fukkit.mixin.net.minecraft.block;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.WeightedPressurePlateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin (WeightedPressurePlateBlock.class)
public abstract class WeightedPressurePlateBlockMixin extends AbstractPressurePlateBlock {
	@Shadow
	@Final
	private int weight;

	protected WeightedPressurePlateBlockMixin(Settings settings) {
		super(settings);
	}

	@Redirect (method = "getRedstoneOutput(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I",
	           at = @At (value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
	private int fukkit_interactEvent(int a, int b, World world, BlockPos pos) {
		int i = 0;
		for (Entity entity : world.getNonSpectatingEntities(Entity.class, BOX.offset(pos))) {
			Cancellable cancellable;

			if (entity instanceof PlayerEntity) {
				cancellable = CraftEventFactory
				              .callPlayerInteractEvent((PlayerEntity) entity, Action.PHYSICAL, pos, null, null, null);
			} else {
				cancellable = new EntityInteractEvent(((EntityAccess<?>) entity).getBukkit(), ((WorldAccess) world)
				                                                                              .getBukkit()
				                                                                              .getBlockAt(pos
				                                                                                          .getX(), pos
				                                                                                                   .getY(), pos
				                                                                                                            .getZ()));
				((WorldAccess) world).getBukkitServer().getPluginManager().callEvent((EntityInteractEvent) cancellable);
			}

			// We only want to block turning the plate on if all events are cancelled
			if (!cancellable.isCancelled()) {
				i++;
			}
		}

		return Math.min(i, this.weight);
	}
}
