package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BannerBlockEntity.class)
public class BannerBlockEntityMixin {
	@Shadow @Nullable private ListTag patternListTag;

	@Inject (method = "fromTag", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/BannerBlockEntity;patterns:Ljava/util/List;"))
	private void fukkit_cleanListTag(CompoundTag tag, CallbackInfo ci) {
		while (this.patternListTag.size() > 20)
			this.patternListTag.remove(20);
	}
}
