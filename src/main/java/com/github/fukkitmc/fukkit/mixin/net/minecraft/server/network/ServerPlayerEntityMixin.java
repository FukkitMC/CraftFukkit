package com.github.fukkitmc.fukkit.mixin.net.minecraft.server.network;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacketAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayNetworkHandlerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ServerWorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.PortalForcerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import com.github.fukkitmc.fukkit.mixin.net.minecraft.entity.player.PlayerEntityMixin;
import com.github.fukkitmc.fukkit.util.Constructors;
import com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.block.BukkitChestDoubleInventory;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerListener;
import net.minecraft.container.HorseContainer;
import net.minecraft.container.NameableContainerFactory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.MainHand;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Consumer;

@Implements (@Interface (iface = ServerPlayerEntityAccess.class, prefix = "fukkit$"))
@Mixin (ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin {

	private static ThreadLocal<Container> tempContainer;
	@Shadow
	public ServerPlayNetworkHandler networkHandler;
	@Shadow
	@Final
	public MinecraftServer server;
	@Shadow
	@Final
	public ServerPlayerInteractionManager interactionManager;
	@Shadow
	public boolean notInAnyWorld;
	public String displayName;
	public Text listName;
	public org.bukkit.Location compassTarget;
	public int newExp = 0;
	public int newLevel = 0;
	public int newTotalExp = 0;
	public boolean keepLevel = false;
	public double maxHealthCache;
	public boolean joining = true;
	public boolean sentListPacket = false;
	public Integer clientViewDistance;
	// CraftBukkit start - Add per-player time and weather.
	public long timeOffset = 0;
	public boolean relativeTime = true;
	public WeatherType weather = null;
	@Shadow
	private boolean inTeleportationState;
	@Shadow
	private int syncedExperience;
	@Shadow
	private float syncedHealth;
	@Shadow
	private int syncedFoodLevel;
	@Shadow
	private Vec3d enteredNetherPos;
	@Shadow
	private boolean seenCredits;
	@Shadow
	private int containerSyncId;
	@Shadow
	private String clientLanguage;
	@Shadow
	private Entity cameraEntity;
	// assert main thread
	private Text component;
	private boolean keepInv;
	private float pluginRainPosition;
	private float pluginRainPositionPrevious;

	@ModifyConstant (method = "<init>", constant = @Constant (stringValue = "en_US"))
	private String fukkit_lowercase(String current) { // is this how it works? idfk
		return "en_us";
	}

	@Inject (method = "<init>", at = @At ("TAIL"))
	private void fukkit_init(MinecraftServer server, ServerWorld world, GameProfile profile,
	                         ServerPlayerInteractionManager interactionManager, CallbackInfo ci) {
		this.displayName = this.getEntityName();
		//this.can
	}

	@Inject (method = "readCustomDataFromTag",
	         at = @At (value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSleeping()Z"))
	private void fukkit_readExtra(CompoundTag tag, CallbackInfo ci) {
		((ServerPlayerEntityAccess) this).getBukkit().readExtraData(tag);
	}

	@Redirect (method = "writeCustomDataToTag",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasPlayerRider()Z"))
	private boolean fukkit_persistVehicle(Entity entity) {
		boolean persist = true;
		Entity thisVehicle = this.getVehicle();
		if (thisVehicle != null) {
			Entity vehicle;
			for (vehicle = thisVehicle; vehicle != null; vehicle = vehicle.getVehicle()) {
				if (!((EntityAccess) vehicle).willPersist()) {
					persist = false;
					break;
				}
			}
		}
		return persist && entity.hasPlayerRider();
	}

	@Inject (method = "writeCustomDataToTag", at = @At ("TAIL"))
	private void fukkit_extraData(CompoundTag tag, CallbackInfo ci) {
		((ServerPlayerEntityAccess) this).getBukkit().setExtraData(tag);
	}

	@Inject (method = "tick", at = @At ("HEAD"))
	private void fukkit_isJoining(CallbackInfo ci) {
		if (this.fukkit$isJoining()) { this.fukkit$setIsJoining(false); }
	}

	@Redirect (method = "playerTick",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getHealth()F",
	                     ordinal = 1))
	private float fukkit_scaleHealth(ServerPlayerEntity entity) {
		return ((CraftPlayer) this.fukkit$getBukkit()).getScaledHealth();
	}

	@Inject (method = "playerTick",
	         at = @At (value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;experienceLevel:I",
	                   ordinal = 0))
	private void fukkit_forceMaxHealthUpdates(CallbackInfo ci) {
		if (this.maxHealthCache != this.getMaximumHealth()) {
			((CraftPlayer) this.fukkit$getBukkit()).updateScaledHealth();
		}
	}

	// imperfect inject
	@Inject (method = "playerTick",
	         at = @At (value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;age:I"))
	private void fukkit_oldLevel(CallbackInfo ci) {
		if (this.oldLevel == -1) { this.oldLevel = this.experienceLevel; }

		if (this.oldLevel != this.experienceLevel) {
			CraftEventFactory.callPlayerLevelChangeEvent(((WorldAccess) this.world).getBukkitServer()
			                                                                       .getPlayer((ServerPlayerEntity) (Object) this), this.oldLevel, this.experienceLevel);
			this.oldLevel = this.experienceLevel;
		}
	}

	@Redirect (method = "updateScores", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/scoreboard/Scoreboard;forEachScore" +
	                                                       "(Lnet/minecraft/scoreboard/ScoreboardCriterion;" +
	                                                       "Ljava/lang/String;Ljava/util/function/Consumer;)V"))
	private void fukkit_useBukkitScores(Scoreboard scoreboard, ScoreboardCriterion criterion, String player,
	                                    Consumer<ScoreboardPlayerScore> action) {
		((WorldAccess) this).getBukkitServer().getScoreboardManager().getScoreboardScores(criterion, player, action);
	}

	// not vanilla?
	@Inject (method = "onDeath", at = @At ("HEAD"))
	private void fukkit_removed(DamageSource source, CallbackInfo ci) {
		if (this.removed) { ci.cancel(); }
	}

	@ModifyVariable (method = "onDeath", at = @At (value = "STORE", ordinal = 0))
	private boolean fukkit_deathEvent(boolean val, DamageSource source) {
		java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<>(this.inventory.getInvSize());
		boolean keepInventory = this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || this.isSpectator();

		if (!keepInventory) {
			for (ItemStack item : ((InventoryAccess) this.inventory).getContents()) {
				if (!item.isEmpty() && !EnchantmentHelper.hasVanishingCurse(item)) {
					loot.add(CraftItemStack.asCraftMirror(item));
				}
			}
		}
		// SPIGOT-5071: manually add player loot tables (SPIGOT-5195 - ignores keepInventory rule)
		this.dropLoot(source, this.playerHitTimer > 0);
		loot.addAll(this.fukkit$getDrops());
		this.fukkit$getDrops().clear(); // SPIGOT-5188: make sure to clear

		Text defaultMessage = this.getDamageTracker().getDeathMessage();

		String deathmessage = defaultMessage.getString();
		org.bukkit.event.entity.PlayerDeathEvent event = CraftEventFactory
		                                                 .callPlayerDeathEvent((ServerPlayerEntity) (Object) this,
		                                                 loot, deathmessage, keepInventory);

		// SPIGOT-943 - only call if they have an inventory open
		if (this.container != this.playerContainer) {
			this.closeContainer();
		}

		String deathMessage = event.getDeathMessage();
		this.keepInv = event.getKeepInventory();
		if (deathMessage != null && deathMessage.isEmpty() && val) {
			if (deathmessage.equals(deathMessage)) {
				this.component = this.getDamageTracker().getDeathMessage();
			} else {
				this.component = CraftChatMessage.fromStringOrNull(deathMessage);
			}
			return true;
		} else { return false; }
	}

	@Shadow
	public abstract boolean isSpectator();

	@Shadow
	public abstract void closeContainer();

	@Redirect (method = "onDeath", at = @At (value = "INVOKE",
	                                         target = "Lnet/minecraft/entity/damage/DamageTracker;getDeathMessage()" +
	                                                  "Lnet/minecraft/text/Text;"))
	private Text fukkit_redir(DamageTracker tracker) {
		Text text = this.component;
		this.component = null;
		return text;
	}

	@Redirect (method = "onDeath",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
	private boolean fukkit_cleanInv(ServerPlayerEntity entity) {
		this.dropXp();
		return this.keepInv;
	}

	@Redirect (method = "onDeath", at = @At (value = "INVOKE",
	                                         target = "Lnet/minecraft/server/network/ServerPlayerEntity;drop" +
	                                                  "(Lnet/minecraft/entity/damage/DamageSource;)V"))
	private void fukkit_invClear(ServerPlayerEntity entity, DamageSource source) {
		this.inventory.clear();
	}

	@Redirect (method = "onDeath", at = @At (value = "INVOKE",
	                                         target = "Lnet/minecraft/scoreboard/Scoreboard;forEachScore" +
	                                                  "(Lnet/minecraft/scoreboard/ScoreboardCriterion;" +
	                                                  "Ljava/lang/String;Ljava/util/function/Consumer;)V"))
	private void fukkit_remSpecTarget(Scoreboard scoreboard, ScoreboardCriterion criterion, String player,
	                                  Consumer<ScoreboardPlayerScore> action) {
		this.setCameraEntity((Entity) (Object) this);
		((WorldAccess) this.world).getBukkitServer().getScoreboardManager()
		                          .getScoreboardScores(criterion, player, action);
	}

	@Shadow
	public abstract void setCameraEntity(Entity entity);

	// two birds 1 stone
	@Redirect (method = "updateKilledAdvancementCriterion", at = @At (value = "INVOKE",
	                                                                  target = "Lnet/minecraft/scoreboard/Scoreboard;" +
	                                                                           "forEachScore(Lnet/minecraft/scoreboard" +
	                                                                           "/ScoreboardCriterion;" +
	                                                                           "Ljava/lang/String;" +
	                                                                           "Ljava/util/function/Consumer;)V"))
	private void fukkit_useBukkitScore(Scoreboard scoreboard, ScoreboardCriterion criterion, String player,
	                                   Consumer<ScoreboardPlayerScore> action) {
		((WorldAccess) this.world).getBukkitServer().getScoreboardManager()
		                          .getScoreboardScores(criterion, player, action);
	}

	@Redirect (method = "updateScoreboardScore", at = @At (value = "INVOKE",
	                                                       target = "Lnet/minecraft/scoreboard/Scoreboard;forEachScore" +
	                                                                "(Lnet/minecraft/scoreboard/ScoreboardCriterion;" +
	                                                                "Ljava/lang/String;Ljava/util/function/Consumer;)" +
	                                                                "V"))
	private void fukkit_useBukkitScore0(Scoreboard scoreboard, ScoreboardCriterion criterion, String player,
	                                    Consumer<ScoreboardPlayerScore> action) {
		((WorldAccess) this.world).getBukkitServer().getScoreboardManager()
		                          .getScoreboardScores(criterion, player, action);
	}

	@Redirect (method = "isPvpEnabled",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isPvpEnabled()Z"))
	private boolean fukkit_worldPvp(MinecraftServer server) {
		return ((WorldAccess) this.world).hasPvp();
	}

	/**
	 * todo replace with injects
	 *
	 * @author HalfOf2
	 * @reason method sig change
	 */
	@SuppressWarnings ("OverwriteModifiers")
	@Overwrite
	public Entity changeDimension(DimensionType type) {
		return this.fukkit$changeDimension(type, PlayerTeleportEvent.TeleportCause.UNKNOWN);
	}

	@Shadow
	public abstract ServerWorld getServerWorld();

	@Override
	public void setWorld(World world) {
		super.setWorld(world);
		if (world == null) {
			this.removed = false;
			Vec3d pos = null;
			if (this.spawnWorld != null && !"".equals(this.spawnWorld)) {
				CraftWorld cworld = (CraftWorld) Bukkit.getServer().getWorld(this.spawnWorld);
				if (cworld != null && this.getSpawnPosition() != null) {
					world = cworld.getHandle();
					pos = PlayerEntity.findRespawnPosition(cworld.getHandle(), this.getSpawnPosition(), false)
					                  .orElse(null);
				}
			}

			if (world == null || pos == null) {
				world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
				pos = new Vec3d(world.getSpawnPos()); // todo verify
			}
			this.world = world;
			this.updatePosition(pos.getX(), pos.getY(), pos.getZ());
		}

		this.dimension = ((ServerWorld) this.world).dimension.getType();
		this.interactionManager.setWorld((ServerWorld) world);
	}

	@Shadow
	protected abstract void dimensionChanged(ServerWorld targetWorld);

	@Inject (method = "wakeUp", at = @At ("HEAD"), cancellable = true)
	private void fukkit_isSleeping(boolean bl, boolean bl2, CallbackInfo ci) {
		if (!this.isSleeping()) { ci.cancel(); }
	}

	@ModifyVariable (
	method = "openContainer(Lnet/minecraft/container/NameableContainerFactory;)Ljava/util/OptionalInt;",
	at = @At (value = "INVOKE_ASSIGN",
	          target = "Lnet/minecraft/container/NameableContainerFactory;createMenu" +
	                   "(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/entity/player/PlayerEntity;)" +
	                   "Lnet/minecraft/container/Container;"))
	private Container fukkit_invOpenHook(Container container, NameableContainerFactory containerFactory) {
		if (container != null) {
			((ContainerAccess) container).setTitle(containerFactory.getDisplayName());
			boolean cancelled = false;
			container = CraftEventFactory
			            .callInventoryOpenEvent((ServerPlayerEntity) (Object) this, container, cancelled);
			if (container == null && !cancelled) { // for your reference md5.. booleans are not in fact passed by
				// reference..
				if (containerFactory instanceof Inventory) {
					((Inventory) containerFactory).onInvClose((ServerPlayerEntity) (Object) this);
				} else if (containerFactory instanceof BukkitChestDoubleInventory) {
					((BukkitChestDoubleInventory) containerFactory).inventory
					.onInvClose((ServerPlayerEntity) (Object) this);
				}
				// imperfect bukkit behavior, but I'm way too lazy to deal with it
				return null;
			}
		}
		return container;
	}

	@Redirect (method = "openContainer(Lnet/minecraft/container/NameableContainerFactory;)Ljava/util/OptionalInt;",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/container/Container;addListener" +
	                              "(Lnet/minecraft/container/ContainerListener;)V"))
	private void fukkit_moveDown(Container container, ContainerListener listener) {}

	@Redirect (method = "openContainer(Lnet/minecraft/container/NameableContainerFactory;)Ljava/util/OptionalInt;",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket" +
	                              "(Lnet/minecraft/network/Packet;)V"))
	private void fukkit_containerTitle(ServerPlayNetworkHandler handler, Packet<?> packet) {}

	@Inject (method = "openContainer(Lnet/minecraft/container/NameableContainerFactory;)Ljava/util/OptionalInt;",
	         at = @At (value = "RETURN", ordinal = 0), slice = @Slice (from = @At (value = "INVOKE",
	                                                                               target = "Lnet/minecraft/container" +
	                                                                                        "/Container;addListener" +
	                                                                                        "(Lnet/minecraft/container" +
	                                                                                        "/ContainerListener;)V")),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_containerTitle(NameableContainerFactory nameableContainerFactory,
	                                   CallbackInfoReturnable<OptionalInt> cir, Container container) {
		this.networkHandler
		.sendPacket(new OpenContainerS2CPacket(container.syncId, container.getType(), ((ContainerAccess) container)
		                                                                              .getTitle()));
		container.addListener((ContainerListener) this);
	}

	@Inject (method = "openHorseInventory", at = @At ("HEAD"), cancellable = true)
	private void fukkit_openHorseEvent(HorseBaseEntity horseBaseEntity, Inventory inventory, CallbackInfo ci) {
		this.incrementContainerSyncId();
		Container container = new HorseContainer(this.containerSyncId, this.inventory, inventory, horseBaseEntity);
		((ContainerAccess) container).setTitle(horseBaseEntity.getDisplayName());
		container = CraftEventFactory.callInventoryOpenEvent((ServerPlayerEntity) (Object) this, container);

		if (container == null) {
			inventory.onInvClose((ServerPlayerEntity) (Object) this);
			ci.cancel();
		} else { tempContainer.set(container); }
	}

	@Shadow
	protected abstract void incrementContainerSyncId();

	@Redirect (method = "openHorseInventory",
	           at = @At (value = "NEW", target = "net/minecraft/container/HorseContainer"))
	private HorseContainer fukkit_horseContainer(int syncId, PlayerInventory playerInventory, Inventory inventory,
	                                             HorseBaseEntity horseBaseEntity) {
		return null; // pranked
	}

	@Inject (method = "openHorseInventory", at = @At (value = "INVOKE",
	                                                  target = "Lnet/minecraft/container/Container;addListener" +
	                                                           "(Lnet/minecraft/container/ContainerListener;)V"))
	private void fukkit_setcontainer(HorseBaseEntity horseBaseEntity, Inventory inventory, CallbackInfo ci) {
		this.container = tempContainer.get();
		tempContainer.set(null);
	}

	@Redirect (method = "openHorseInventory", at = @At (value = "INVOKE",
	                                                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;" +
	                                                             "incrementContainerSyncId()V"))
	private void fukkit_movedUp(ServerPlayerEntity entity) {}

	@Inject (method = "onContainerRegistered", at = @At ("TAIL"))
	private void fukkit_updateResult(Container container, DefaultedList<ItemStack> defaultedList, CallbackInfo ci) {
		if (java.util.EnumSet.of(InventoryType.CRAFTING, InventoryType.WORKBENCH)
		                     .contains(((ContainerAccess) container).getBukkitView().getType())) {
			this.networkHandler
			.sendPacket(new ContainerSlotUpdateS2CPacket(container.syncId, 0, container.getSlot(0).getStack()));
		}
	}

	@Inject (method = "closeContainer", at = @At ("HEAD"))
	private void fukkit_handleCloseEvent(CallbackInfo ci) {
		CraftEventFactory.handleInventoryCloseEvent((ServerPlayerEntity) (Object) this);
	}

	@SuppressWarnings ("InvalidMemberReference")
	@Redirect (method = {"increaseStat", "resetStat"}, at = @At (value = "INVOKE",
	                                                             target = "Lnet/minecraft/scoreboard/Scoreboard;" +
	                                                                      "forEachScore(Lnet/minecraft/scoreboard" +
	                                                                      "/ScoreboardCriterion;Ljava/lang/String;" +
	                                                                      "Ljava/util/function/Consumer;)V"))
	private void fukkit_bukkitScores(Scoreboard scoreboard, ScoreboardCriterion criterion, String player,
	                                 Consumer<ScoreboardPlayerScore> action) {
		((WorldAccess) this.world).getBukkitServer().getScoreboardManager()
		                          .getScoreboardScores(criterion, player, action);
	}

	@Inject (method = "markHealthDirty", at = @At ("TAIL"))
	private void fukkit_addedToReset(CallbackInfo ci) {
		this.syncedExperience = -1;
	}

	@Redirect (method = "copyFrom", at = @At (value = "INVOKE",
	                                          target = "Lnet/minecraft/server/network/ServerRecipeBook;copyFrom" +
	                                                   "(Lnet/minecraft/recipe/book/RecipeBook;)V"))
	private void fukkit_voidCall(ServerRecipeBook book, RecipeBook book2) {}

	@Inject (method = "copyFrom", at = @At ("TAIL"))
	private void fukkit_spigot4767(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		this.inLava = false;
	}

	@Inject (method = "setGameMode", at = @At ("HEAD"), cancellable = true)
	private void fukkit_gameModeChangeEvent(GameMode gameMode, CallbackInfo ci) {
		if (gameMode == this.interactionManager.getGameMode()) {
			return;
		}

		PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent((Player) this
		                                                                         .fukkit$getBukkit(),
		org.bukkit.GameMode
		                                                                                              .getByValue(gameMode
		                                                                                                          .getId()));
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject (method = "setClientSettings", at = @At ("HEAD"))
	private void fukkit_changeHandEvent(ClientSettingsC2SPacket packet, CallbackInfo ci) {
		if (this.getMainArm() != packet.getMainArm()) {
			PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent((Player) this.fukkit$getBukkit(),
			this.getMainArm() == Arm.LEFT ? MainHand.LEFT : MainHand.RIGHT);
			((WorldAccess) this.server).getBukkitServer().getPluginManager().callEvent(event);
		}
		if (!this.clientLanguage.equals(packet.getLanguage())) {
			PlayerLocaleChangeEvent event = new PlayerLocaleChangeEvent((Player) this.fukkit$getBukkit(), packet
			                                                                                              .getLanguage());
			((WorldAccess) this.server).getBukkitServer().getPluginManager().callEvent(event);
		}
		this.clientViewDistance = ((ClientSettingsC2SPacketAccess) packet).getViewDistance();
	}

	@Redirect (method = "setCameraEntity", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/server/network/ServerPlayerEntity;" +
	                                                          "requestTeleport(DDD)V"))
	private void fukkit_teleCause(ServerPlayerEntity entity, double destX, double destY, double destZ) {
		((ServerPlayNetworkHandlerAccess) this.networkHandler)
		.teleportRequest(this.cameraEntity.getX(), this.cameraEntity.getY(), this.cameraEntity
		                                                                     .getZ(), this.yaw, this.pitch,
		PlayerTeleportEvent.TeleportCause.SPECTATE);
	}

	@Inject (method = "method_14206", at = @At ("RETURN"), cancellable = true)
	private void fukkit_listName(CallbackInfoReturnable<Text> cir) {
		cir.setReturnValue(this.listName);
	}

	/**
	 * unlikely to be replaced with injects
	 *
	 * @author HalfOf2
	 * @reason method redirect
	 */
	@Overwrite
	public void teleport(ServerWorld world, double x, double y, double z, float yaw, float pitch) {
		this.fukkit$teleport(world, x, y, z, yaw, pitch, PlayerTeleportEvent.TeleportCause.UNKNOWN);
	}

	@Shadow
	public abstract void stopRiding();

	public long getPlayerTime() {
		if (this.relativeTime) {
			// Adds timeOffset to the current server time.
			return this.world.getTimeOfDay() + this.timeOffset;
		} else {
			// Adds timeOffset to the beginning of this day.
			return this.world.getTimeOfDay() - (this.world.getTimeOfDay() % 24000) + this.timeOffset;
		}
	}

	public WeatherType getPlayerWeather() {
		return this.weather;
	}

	public void updateWeather(float oldRain, float newRain, float oldThunder, float newThunder) {
		if (this.weather == null) {
			// Vanilla
			if (oldRain != newRain) {
				this.networkHandler.sendPacket(new GameStateChangeS2CPacket(7, newRain));
			}
		} else {
			// Plugin
			if (this.pluginRainPositionPrevious != this.pluginRainPosition) {
				this.networkHandler.sendPacket(new GameStateChangeS2CPacket(7, this.pluginRainPosition));
			}
		}

		if (oldThunder != newThunder) {
			if (this.weather == WeatherType.DOWNFALL || this.weather == null) {
				this.networkHandler.sendPacket(new GameStateChangeS2CPacket(8, newThunder));
			} else {
				this.networkHandler.sendPacket(new GameStateChangeS2CPacket(8, 0));
			}
		}
	}

	public void tickWeather() {
		if (this.weather == null) { return; }

		this.pluginRainPositionPrevious = this.pluginRainPosition;
		if (this.weather == WeatherType.DOWNFALL) {
			this.pluginRainPosition += 0.01;
		} else {
			this.pluginRainPosition -= 0.01;
		}

		this.pluginRainPosition = MathHelper.clamp(this.pluginRainPosition, 0.0F, 1.0F);
	}

	public void resetPlayerWeather() {
		this.weather = null;
		this.setPlayerWeather(
		this.world.getLevelProperties().isRaining() ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
	}

	public void setPlayerWeather(WeatherType type, boolean plugin) {
		if (!plugin && this.weather != null) {
			return;
		}

		if (plugin) {
			this.weather = type;
		}

		if (type == WeatherType.DOWNFALL) {
			this.networkHandler.sendPacket(new GameStateChangeS2CPacket(2, 0));
		} else {
			this.networkHandler.sendPacket(new GameStateChangeS2CPacket(1, 0));
		}
	}

	@Override
	public String toString() {
		return super.toString() + "(" + this.getEntityName() + " at " + this.getX() + "," + this.getY() + "," + this
		                                                                                                        .getZ() + ")";
	}

	// SPIGOT-1903, MC-98153
	public void forceSetPositionRotation(double x, double y, double z, float yaw, float pitch) {
		this.refreshPositionAndAngles(x, y, z, yaw, pitch);
		this.networkHandler.syncWithPlayerPosition();
	}

	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || !((Player) this.fukkit$getBukkit()).isOnline();
	}

	@Override
	public Scoreboard getScoreboard() {
		return ((CraftPlayer) this.fukkit$getBukkit()).getScoreboard().getHandle();
	}

	/**
	 * todo replace with injects
	 *
	 * @author HalfOf2
	 * @reason method function overriden
	 */
	@Override
	@Overwrite
	public Either<PlayerEntity.SleepFailureReason, Unit> trySleep(BlockPos pos) {
		return this.fukkit$sleep(pos, false);
	}

	@Override
	public Either<PlayerEntity.SleepFailureReason, Unit> fukkit$sleep(BlockPos blockPos, boolean force) {
		return super.fukkit$sleep(blockPos, force).ifRight((unit) -> {
			this.incrementStat(Stats.SLEEP_IN_BED);
			Criterions.SLEPT_IN_BED.trigger((ServerPlayerEntity) (Object) this);
		});
	}

	public String fukkit$getDisplayName() {
		return this.displayName;
	}

	public Text fukkit$getListName() {
		return this.listName;
	}

	public Location fukkit$getCompassTarget() {
		return this.compassTarget;
	}

	public int fukkit$getNewExp() {
		return this.newExp;
	}

	public int fukkit$getNewLevel() {
		return this.newLevel;
	}

	public int fukkit$getNewTotalExp() {
		return this.newTotalExp;
	}

	public boolean fukkit$shouldKeepLevel() {
		return this.keepLevel;
	}

	public double fukkit$getMaxHealthCache() {
		return this.maxHealthCache;
	}

	public boolean fukkit$isJoining() {
		return this.joining;
	}

	public boolean fukkit$hasSentListPacket() {
		return this.sentListPacket;
	}

	public int fukkit$getClientViewDistance() {
		return this.clientViewDistance;
	}

	public void fukkit$setDisplayName(String name) {
		this.displayName = name;
	}

	public void fukkit$setListName(Text listName) {
		this.listName = listName;
	}

	public void fukkit$setCompassTarget(Location location) {
		this.compassTarget = location;
	}

	public void fukkit$setNewExp(int exp) {
		this.newExp = exp;
	}

	public void fukkit$setNewLevel(int level) {
		this.newLevel = level;
	}

	public void fukkit$setNewTotalExp(int exp) {
		this.newTotalExp = exp;
	}

	public void fukkit$setKeepLevel(boolean shouldKeepLevel) {
		this.keepLevel = shouldKeepLevel;
	}

	public void fukkit$setMaxHealthCache(double health) {
		this.maxHealthCache = health;
	}

	public void fukkit$setIsJoining(boolean isJoining) {
		this.joining = isJoining;
	}

	public void fukkit$setHasSentListPacket(boolean hasSentListPacket) {
		this.sentListPacket = hasSentListPacket;
	}

	public void fukkit$setClientViewDistance(int distance) {
		this.clientViewDistance = distance;
	}

	public final BlockPos fukkit$getSpawnPoint(ServerWorld worldserver) {
		BlockPos blockposition = worldserver.getSpawnPos();

		if (worldserver.dimension.isNether() && worldserver.getLevelProperties()
		                                                   .getGameMode() != net.minecraft.world.GameMode.ADVENTURE) {
			int i = Math.max(0, this.server.getSpawnRadius(worldserver));
			int j = MathHelper.floor(worldserver.getWorldBorder()
			                                    .getDistanceInsideBorder(blockposition.getX(), blockposition.getZ()));

			if (j < i) {
				i = j;
			}

			if (j <= 1) {
				i = 1;
			}

			int k = (i * 2 + 1) * (i * 2 + 1);
			int l = this.method_14244(k);
			int i1 = (new Random()).nextInt(k);

			for (int j1 = 0; j1 < k; ++j1) {
				int k1 = (i1 + l * j1) % k;
				int l1 = k1 % (i * 2 + 1);
				int i2 = k1 / (i * 2 + 1);
				BlockPos blockposition1 = worldserver.getDimension().getTopSpawningBlockPosition(blockposition
				                                                                                 .getX() + l1 - i,
				blockposition
				                                                                                                   .getZ() + i2 - i, false);

				if (blockposition1 != null) {
					return blockposition1;
				}
			}
		}

		return blockposition;
	}

	@Shadow
	protected abstract int method_14244(int i);

	public Entity fukkit$changeDimension(DimensionType type, PlayerTeleportEvent.TeleportCause cause) {
		// CraftBukkit end
		if (this.isSleeping()) {
			return (Entity) (Object) this; // CraftBukkit - SPIGOT-3154
		}
		// this.worldChangeInvuln = true; // CraftBukkit - Moved down and into PlayerList#changeDimension
		DimensionType currentDimensionType = this.dimension;

		if (((DimensionTypeAccess) currentDimensionType)
		    .getType() == DimensionType.THE_END && ((DimensionTypeAccess) type)
		                                           .getType() == DimensionType.OVERWORLD) { // CraftBukkit - getType()
			this.inTeleportationState = true; // CraftBukkit - Moved down from above
			this.detach();
			this.getServerWorld().removePlayer((ServerPlayerEntity) (Object) this);
			if (!this.notInAnyWorld) {
				this.notInAnyWorld = true;
				this.networkHandler.sendPacket(new GameStateChangeS2CPacket(4, this.seenCredits ? 0.0F : 1.0F));
				this.seenCredits = true;
			}

		} else {
			ServerWorld currentWorld = this.server.getWorld(currentDimensionType);

			// this.dimension = dimensionmanager; // CraftBukkit
			ServerWorld newWorld = this.server.getWorld(type);
			double currentX = this.getX();
			double currentY = this.getY();
			double currentZ = this.getZ();
			float currentPitch = this.pitch;
			float currentYaw = this.yaw;
			float tempYaw = currentYaw;

			currentWorld.getProfiler().push("moving");
			if (newWorld != null) {
				if (currentDimensionType == DimensionType.OVERWORLD && type == DimensionType.THE_NETHER) {
					this.enteredNetherPos = this.getPos();
					currentX /= 8.0D;
					currentZ /= 8.0D;
				} else if (currentDimensionType == DimensionType.THE_NETHER && type == DimensionType.OVERWORLD) {
					currentX *= 8.0D;
					currentZ *= 8.0D;
				} else if (currentDimensionType == DimensionType.OVERWORLD && type == DimensionType.THE_END) {
					BlockPos forcedSpawn = newWorld.getForcedSpawnPoint();

					currentX = forcedSpawn.getX();
					currentY = forcedSpawn.getY();
					currentZ = forcedSpawn.getZ();
					currentYaw = 90.0F;
					currentPitch = 0.0F;
				}
			}  // CraftBukkit - empty to fall through to null to event


			// CraftBukkit start
			Location enter = ((Player) this.fukkit$getBukkit()).getLocation();
			Location exit = (newWorld == null) ? null : new Location(((WorldAccess) newWorld)
			                                                         .getBukkit(), currentX, currentY, currentZ,
			currentYaw, currentPitch);
			PlayerPortalEvent event = new PlayerPortalEvent((Player) this
			                                                         .fukkit$getBukkit(), enter, exit, cause, 128,
			true,
			((DimensionTypeAccess) type).getType() == DimensionType.THE_END ? 0 : 16);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled() || event.getTo() == null) {
				return null;
			}

			exit = event.getTo();
			if (exit == null) {
				return null;
			}
			newWorld = ((CraftWorld) exit.getWorld()).getHandle();
			currentX = exit.getX();
			currentY = exit.getY();
			currentZ = exit.getZ();
			// CraftBukkit end

			// this.setPositionRotation(d0, d1, d2, f1, f); // CraftBukkit - PlayerTeleportEvent handles position
			// changes
			currentWorld.getProfiler().pop();
			currentWorld.getProfiler().push("placing");
			double worldBoundWest = Math.min(-2.9999872E7D, newWorld.getWorldBorder().getBoundWest() + 16.0D);
			double worldBoundNorth = Math.min(-2.9999872E7D, newWorld.getWorldBorder().getBoundNorth() + 16.0D);
			double worldBoundEast = Math.min(2.9999872E7D, newWorld.getWorldBorder().getBoundEast() - 16.0D);
			double worldBoundSouth = Math.min(2.9999872E7D, newWorld.getWorldBorder().getBoundSouth() - 16.0D);

			currentX = MathHelper.clamp(currentX, worldBoundWest, worldBoundEast);
			currentZ = MathHelper.clamp(currentZ, worldBoundNorth, worldBoundSouth);
			// this.setPositionRotation(d0, d1, d2, f1, f); // CraftBukkit - PlayerTeleportEvent handles position
			// changes
			// CraftBukkit start - PlayerPortalEvent implementation
			Vec3d exitVelocity;
			BlockPos exitPosition = new BlockPos(currentX, currentY, currentZ);
			if (((DimensionTypeAccess) type).getType() == DimensionType.THE_END) { // CraftBukkit - getType()
				int exitPositionX = exitPosition.getX();
				int exitPositionY = exitPosition.getY() - 1;
				int exitPositionZ = exitPosition.getZ();
				if (event.getCanCreatePortal()) {
					// CraftBukkit end
					org.bukkit.craftbukkit.util.BlockStateListPopulator blockList =
					new org.bukkit.craftbukkit.util.BlockStateListPopulator(newWorld); // CraftBukkit - Use
					// BlockStateListPopulator
					for (int zOffset = -2; zOffset <= 2; ++zOffset) {
						for (int xOffset = -2; xOffset <= 2; ++xOffset) {
							for (int yOffset = -1; yOffset < 3; ++yOffset) {
								int blockX = exitPositionX + xOffset;
								int blockY = exitPositionY + yOffset;
								int blockZ = exitPositionZ - zOffset;
								boolean placeObsidian = yOffset < 0;

								blockList.setBlockState(new BlockPos(blockX, blockY, blockZ),
								placeObsidian ? Blocks.OBSIDIAN.getDefaultState() :
								Blocks.AIR.getDefaultState(), 3); // CraftBukkit
							}
						}
					}

					// CraftBukkit start
					org.bukkit.World bukkitWorld = ((ServerWorldAccess) newWorld).getBukkit();
					org.bukkit.event.world.PortalCreateEvent portalEvent =
					new org.bukkit.event.world.PortalCreateEvent((List<BlockState>) (List) blockList
					                                                                                                                              .getList(), bukkitWorld, (Player) this
					                                                                                                                                                                .fukkit$getBukkit(), org.bukkit.event.world.PortalCreateEvent.CreateReason.END_PLATFORM);

					((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(portalEvent);
					if (!portalEvent.isCancelled()) {
						blockList.updateList();
					}
				}
				// handled below for PlayerTeleportEvent
				// this.setPositionRotation((double) i, (double) j, (double) k, f1, 0.0F);
				exit.setX(exitPositionX);
				exit.setY(exitPositionY);
				exit.setZ(exitPositionZ);
				exitVelocity = Vec3d.ZERO;
			} else {
				BlockPattern.TeleportTarget portalShape = ((PortalForcerAccess) newWorld.getPortalForcer())
				                                          .findAndTeleport((ServerPlayerEntity) (Object) this,
				                                          exitPosition, tempYaw, event
				                                                                                                                      .getSearchRadius(), true);
				if (portalShape == null && event.getCanCreatePortal()) {
					if (((PortalForcerAccess) newWorld.getPortalForcer())
					    .createPortal((ServerPlayerEntity) (Object) this, exitPosition, event
					                                                                    .getCreationRadius())) { //
						// Only check for new portal if creation succeeded
						portalShape = ((PortalForcerAccess) newWorld.getPortalForcer())
						              .findAndTeleport((ServerPlayerEntity) (Object) this, exitPosition, tempYaw, event
						                                                                                          .getSearchRadius(), true);
					}
				}
				// Check if portal was found
				if (portalShape == null) {
					return null;
				}
				// Teleport handling - logic from PortalTravelAgent#findAndTeleport
				exitVelocity = portalShape.velocity;
				exit.setX(portalShape.pos.getX());
				exit.setY(portalShape.pos.getY());
				exit.setZ(portalShape.pos.getZ());
				exit.setYaw(tempYaw + (float) portalShape.yaw);
				// CraftBukkit end
			}

			currentWorld.getProfiler().pop();
			// CraftBukkit start - PlayerTeleportEvent
			PlayerTeleportEvent tpEvent = new PlayerTeleportEvent((Player) this.fukkit$getBukkit(), enter, exit,
			cause);
			Bukkit.getServer().getPluginManager().callEvent(tpEvent);
			if (tpEvent.isCancelled() || tpEvent.getTo() == null) {
				return null;
			}

			exit = tpEvent.getTo();
			if (exit == null) {
				return null;
			}
			newWorld = ((CraftWorld) exit.getWorld()).getHandle();
			this.inTeleportationState = true; // CraftBukkit - Set teleport invulnerability only if player changing
			// worlds
			type = newWorld.getDimension().getType();

			this.dimension = type;

			this.networkHandler
			.sendPacket(new PlayerRespawnS2CPacket(((DimensionTypeAccess) newWorld.dimension.getType())
			                                       .getType(), LevelProperties
			                                                   .sha256Hash(this.world.getLevelProperties()
			                                                                         .getSeed()), this.world
			                                                                                      .getLevelProperties()
			                                                                                      .getGeneratorType(),
			this.interactionManager
			                                                                                                           .getGameMode()));
			this.networkHandler
			.sendPacket(new DifficultyS2CPacket(this.world.getDifficulty(), this.world.getLevelProperties()
			                                                                          .isDifficultyLocked()));
			PlayerManager manager = this.server.getPlayerManager();

			manager.sendCommandTree((ServerPlayerEntity) (Object) this);
			currentWorld.removePlayer((ServerPlayerEntity) (Object) this);
			this.removed = false;

			this.setVelocity(exitVelocity);
			// CraftBukkit end
			this.setWorld(newWorld);
			newWorld.onPlayerChangeDimension((ServerPlayerEntity) (Object) this);
			this.dimensionChanged(currentWorld);
			((ServerPlayNetworkHandlerAccess) this.networkHandler)
			.teleport(exit); // CraftBukkit - use internal teleport without event
			this.networkHandler
			.syncWithPlayerPosition(); // CraftBukkit - sync position after changing it (from
			// PortalTravelAgent#findAndteleport)
			this.interactionManager.setWorld(newWorld);
			this.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(this.abilities));
			manager.sendWorldInfo((ServerPlayerEntity) (Object) this, newWorld);
			manager.method_14594((ServerPlayerEntity) (Object) this);

			for (StatusEffectInstance mobeffect : this.getStatusEffects()) {
				this.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getEntityId(), mobeffect));
			}

			this.networkHandler.sendPacket(new WorldEventS2CPacket(1032, BlockPos.ORIGIN, 0, false));
			this.syncedExperience = -1;
			this.syncedHealth = -1.0F;
			this.syncedFoodLevel = -1;

			// CraftBukkit start
			PlayerChangedWorldEvent changeEvent = new PlayerChangedWorldEvent((Player) this
			                                                                           .fukkit$getBukkit(),
			((ServerWorldAccess) currentWorld)
			                                                                                                .getBukkit());
			((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(changeEvent);
			// CraftBukkit end
		}
		return (ServerPlayerEntity) (Object) this;
	}

	public int fukkit$nextSyncId() {
		this.incrementContainerSyncId();
		return this.containerSyncId;
	}

	public void fukkit$sendMessage(Text[] texts) {
		for (Text text : texts) {
			this.sendMessage(text);
		}
	}

	@Shadow
	public abstract void sendMessage(Text message);

	public void fukkit$teleport(ServerWorld world, double x, double y, double z, float yaw, float pitch,
	                            org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
		// CraftBukkit end
		this.setCameraEntity((ServerPlayerEntity) (Object) this);
		this.stopRiding();
		((Player) this.fukkit$getBukkit())
		.teleport(new Location(((WorldAccess) world).getBukkit(), x, y, z, yaw, pitch), cause);
		// CraftBukkit end
	}

	public void fukkit$reset() {
		float exp = 0;
		boolean keepInventory = this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);

		if (this.keepLevel || keepInventory) {
			exp = this.experienceProgress;
			this.newTotalExp = this.totalExperience;
			this.newLevel = this.experienceLevel;
		}

		this.setHealth(this.getMaximumHealth());
		this.setFireTicks(0); //this.fireTicks = 0;
		this.fallDistance = 0;
		this.hungerManager = Constructors.newHungerManager((PlayerEntity) (Object) this);
		this.experienceLevel = this.newLevel;
		this.totalExperience = this.newTotalExp;
		this.experienceProgress = 0;
		this.deathTime = 0;
		this.setStuckArrowCount(0);
		this.fukkit$removeAllEffects(org.bukkit.event.entity.EntityPotionEffectEvent.Cause.DEATH);
		this.markEffectsDirty(); // this.effectsChanged = true;
		this.container = this.playerContainer;
		this.attackingPlayer = null;
		this.fukkit_setAttacker(null); // this.attacker = null;
		this.fukkit_setDamageTracker(new DamageTracker((ServerPlayerEntity) (Object) this));
		this.syncedExperience = -1;
		if (this.keepLevel || keepInventory) {
			this.experienceProgress = exp;
		} else {
			this.addExperience(this.newExp);
		}
		this.keepLevel = false;
	}

	@Shadow public abstract void addExperience(int experience);
}
