package com.github.fukkitmc.fukkit.mixin.net.minecraft.command;

import com.github.fukkitmc.fukkit.access.net.minecraft.command.arguments.EntitySelectorReaderAccess;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.command.EntitySelectorReader.*;

@Implements (@Interface (iface = EntitySelectorReaderAccess.class, prefix = "fukkit$"))
@Mixin (EntitySelectorReader.class)
public abstract class EntitySelectorReaderMixin {

	@Shadow
	private boolean usesAt;

	@Shadow
	private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestionProvider;

	@Shadow
	protected abstract CompletableFuture<Suggestions> suggestSelectorRest(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer);

	@Shadow
	@Final
	private StringReader reader;

	@Shadow
	private int limit;

	@Shadow
	private boolean includesNonPlayers;

	@Shadow
	private BiConsumer<Vec3d, List<? extends Entity>> sorter;

	@Shadow
	public abstract void setEntityType(EntityType<?> entityType);

	@Shadow
	private boolean senderOnly;

	@Shadow
	private Predicate<Entity> predicate;

	@Shadow
	protected abstract CompletableFuture<Suggestions> suggestOpen(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer);

	@Shadow
	protected abstract CompletableFuture<Suggestions> suggestOptionOrEnd(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer);

	@Shadow
	protected abstract void readArguments() throws CommandSyntaxException;

	@Shadow
	private int startCursor;

	@Shadow
	protected abstract CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer);

	@Shadow
	@Final
	private boolean atAllowed;

	@Shadow
	protected abstract void readRegular() throws CommandSyntaxException;

	@Shadow
	protected abstract void buildPredicate();

	@Shadow
	public abstract EntitySelector build();

	public EntitySelector fukkit$parse(boolean overridePermissions) throws CommandSyntaxException {
		this.startCursor = this.reader.getCursor();
		this.suggestionProvider = this::suggestSelector;
		if (this.reader.canRead() && this.reader.peek() == '@') {
			if (!this.atAllowed) {
				throw NOT_ALLOWED_EXCEPTION.createWithContext(this.reader);
			}

			this.reader.skip();
			this.parseSelector(overridePermissions);
		} else {
			this.readRegular();
		}

		this.buildPredicate();
		return this.build();
	}

	@Unique
	public void parseSelector(boolean overridePermissions) throws CommandSyntaxException {
		this.usesAt = !overridePermissions;
		this.suggestionProvider = this::suggestSelectorRest;
		if (!this.reader.canRead()) {
			throw MISSING_EXCEPTION.createWithContext(this.reader);
		} else {
			int i = this.reader.getCursor();
			char c = this.reader.read();
			if (c == 'p') {
				this.limit = 1;
				this.includesNonPlayers = false;
				this.sorter = NEAREST;
				this.setEntityType(EntityType.PLAYER);
			} else if (c == 'a') {
				this.limit = Integer.MAX_VALUE;
				this.includesNonPlayers = false;
				this.sorter = ARBITRARY;
				this.setEntityType(EntityType.PLAYER);
			} else if (c == 'r') {
				this.limit = 1;
				this.includesNonPlayers = false;
				this.sorter = EntitySelectorReader.RANDOM;
				this.setEntityType(EntityType.PLAYER);
			} else if (c == 's') {
				this.limit = 1;
				this.includesNonPlayers = true;
				this.senderOnly = true;
			} else {
				if (c != 'e') {
					this.reader.setCursor(i);
					throw EntitySelectorReader.UNKNOWN_SELECTOR_EXCEPTION.createWithContext(this.reader, '@' + String.valueOf(c));
				}

				this.limit = Integer.MAX_VALUE;
				this.includesNonPlayers = true;
				this.sorter = ARBITRARY;
				this.predicate = Entity::isAlive;
			}

			this.suggestionProvider = this::suggestOpen;
			if (this.reader.canRead() && this.reader.peek() == '[') {
				this.reader.skip();
				this.suggestionProvider = this::suggestOptionOrEnd;
				this.readArguments();
			}
		}
	}
}
