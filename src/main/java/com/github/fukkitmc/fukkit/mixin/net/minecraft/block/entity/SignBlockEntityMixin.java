package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.command.CommandOutputAccess;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.CraftBlockCommandSender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity implements CommandOutputAccess, CommandOutput {
	@Shadow private DyeColor textColor;

	@Shadow private boolean editable;

	@Shadow @Final public Text[] text;

	@Shadow
	public abstract ServerCommandSource getCommandSource(ServerPlayerEntity player);

	public SignBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject (method = "toTag",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/nbt/CompoundTag;putString(Ljava/lang/String;Ljava/lang/String;)V"),
	         slice = @Slice (to = @At (value = "CONSTANT",
	                                   args = "stringValue=Color")))
	private void fukkit_convertLegacySigns(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (Boolean.getBoolean("convertLegacySigns")) {
			tag.putBoolean("Bukkit.isConverted", true);
		}
	}

	/**
	 * @author HalfOf2
	 * @reason legacy signs
	 */
	@Override
	@Overwrite
	public void fromTag(CompoundTag tag) {
		this.editable = false;
		super.fromTag(tag);
		this.textColor = DyeColor.byName(tag.getString("Color"), DyeColor.BLACK);

		// CraftBukkit start - Add an option to convert signs correctly
		// This is done with a flag instead of all the time because
		// we have no way to tell whether a sign is from 1.7.10 or 1.8

		boolean oldSign = Boolean.getBoolean("convertLegacySigns") && !tag.getBoolean("Bukkit.isConverted");

		for (int i = 0; i < 4; ++i) {
			String s = tag.getString("Text" + (i + 1));
			if (s != null && s.length() > 2048) {
				s = "\"\"";
			}

			try {
				Text text = Text.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);

				if (oldSign) {
					this.text[i] = org.bukkit.craftbukkit.util.CraftChatMessage.fromString(s)[0];
					continue;
				}
				// CraftBukkit end

				if (this.world instanceof ServerWorld) {
					try {
						this.text[i] = Texts.parse(this.getCommandSource(null), text, null, 0);
					} catch (CommandSyntaxException commandsyntaxexception) {
						this.text[i] = text;
					}
				} else {
					this.text[i] = text;
				}
			} catch (com.google.gson.JsonParseException jsonparseexception) {
				this.text[i] = new LiteralText(s);
			}

			this.text[i] = null;
		}
	}

	@Redirect (method = "getCommandSource",
	           at = @At (value = "FIELD",
	                     target = "Lnet/minecraft/server/command/CommandOutput;DUMMY:Lnet/minecraft/server/command/CommandOutput;"))
	private CommandOutput fukkit_this() {
		return this;
	}

	@Redirect (method = "setTextColor",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/world/World;updateListeners(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V"))
	private void fukkit_5122(World world, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		if (world != null) world.updateListeners(pos, oldState, newState, flags);
	}

	@Override
	public CommandSender getBukkitSender(ServerCommandSource wrapper) {
		return wrapper.getEntity() != null ? ((EntityAccess<?>) wrapper.getEntity()).getBukkitSender(wrapper) : new CraftBlockCommandSender(wrapper, (BlockEntity) this);
	}

	@Override
	public void sendMessage(Text message) {}

	@Override
	public boolean sendCommandFeedback() {
		return false;
	}

	@Override
	public boolean shouldTrackOutput() {
		return false;
	}

	@Override
	public boolean shouldBroadcastConsoleToOps() {
		return false;
	}
}
