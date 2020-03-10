package com.github.fukkitmc.fukkit.mixin.net.minecraft.server;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.MinecraftServerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.PlayerManagerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerLoginNetworkHandlerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.network.ServerPlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.border.WorldBorderAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import com.github.fukkitmc.fukkit.util.craftbukkit.CraftWorldBorderListener;
import com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.server.BanEntryUtil;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.*;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.UserCache;
import net.minecraft.world.GameRules;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.command.ConsoleCommandCompleter;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * W:arr:NING: CONTAINS CRUST (decompiled mc code)
 */
@Implements (@Interface (iface = PlayerManagerAccess.class, prefix = "fukkit$"))
@Mixin (PlayerManager.class)
public abstract class PlayerManagerMixin {
	// this is not safe, but oh well. most of them don't have any known side effects, but idk
	private static final ThreadLocal<String> PROFILE_NAMES = new ThreadLocal<>();
	private static final ThreadLocal<String> JOIN_MESSAGES = new ThreadLocal<>();
	private static final ThreadLocal<ServerPlayerEntity> SERVER_PLAYER_ENTITY = new ThreadLocal<>();
	private static final ThreadLocal<ServerPlayerEntity> SERVER_PLAYER_ENTITY_2 = new ThreadLocal<>();
	@Shadow
	@Final
	private static Logger LOGGER;
	@Shadow
	@Final
	private static SimpleDateFormat DATE_FORMATTER;
	@Shadow
	@Final
	protected int maxPlayers;
	@Mutable
	@Shadow
	@Final
	private List<ServerPlayerEntity> players;
	@Shadow
	@Final
	private MinecraftServer server;
	@Shadow
	private PlayerSaveHandler saveHandler;
	@Shadow
	@Final
	private Map<UUID, ServerPlayerEntity> playerMap;
	@Shadow
	@Final
	private BannedPlayerList bannedProfiles;
	@Shadow
	@Final
	private BannedIpList bannedIps;
	private CraftServer craftServer;

	@Inject (method = "<init>", at = @At (value = "TAIL"))
	public void init(MinecraftServer server, int maxPlayers, CallbackInfo ci) {
		this.players = new CopyOnWriteArrayList<>();
		MinecraftServerAccess access = (MinecraftServerAccess) server;
		access
		.setBukkit(this.craftServer = new CraftServer((MinecraftDedicatedServer) server,
		(PlayerManager) (Object) this));
		access.setConsoleCommandSender(ColouredConsoleSender.getInstance());
		access.getReader().addCompleter(new ConsoleCommandCompleter(access.getBukkit()));
	}

	@Inject (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/network/ServerPlayerEntity;setWorld" +
	                                                        "(Lnet/minecraft/world/World;)V"),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	public void print(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci, GameProfile gameProfile
	, UserCache userCache, String string, CompoundTag compoundTag, ServerWorld serverWorld) {
		if (compoundTag != null && compoundTag.contains("bukkit")) {
			CompoundTag bukkit = compoundTag.getCompound("bukkit");
			PROFILE_NAMES.set(bukkit.contains("lastKnownName", 8) ? bukkit.getString("lastKnownName") : string);
		}
	}

	@ModifyVariable (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                                       target = "Lnet/minecraft/server/network/ServerPlayerEntity;" +
	                                                                "setWorld(Lnet/minecraft/world/World;)V"),
	                 index = 6, ordinal = 0)
	private String fukkit_renameDetection(String name) {
		String val = PROFILE_NAMES.get();
		if (val != null) {
			PROFILE_NAMES.set(null);
			return val;
		}
		return name;
	}

	@Redirect (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                                 target = "Lorg/apache/logging/log4j/Logger;info" +
	                                                          "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;" +
	                                                          "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;" +
	                                                          "Ljava/lang/Object;)V"))
	private void fukkit_moveMessage(Logger logger, String message, Object p0, Object p1, Object p2, Object p3,
	                                Object p4, Object p5) {}

	@ModifyArg (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                                  target = "Lnet/minecraft/network/packet/s2c/play" +
	                                                           "/GameJoinS2CPacket;<init>" +
	                                                           "(ILnet/minecraft/world/GameMode;" +
	                                                           "JZLnet/minecraft/world/dimension/DimensionType;" +
	                                                           "ILnet/minecraft/world/level/LevelGeneratorType;IZZ)V"),
	            index = 4)
	private DimensionType fukkit_getType(DimensionType type) {
		return ((DimensionTypeAccess) type).getType();
	}

	@Inject (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;" +
	                                                        "sendPacket(Lnet/minecraft/network/Packet;)V",
	                                               ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_sendSupported(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		((CraftPlayer) ((EntityAccess) player).getBukkit()).sendSupportedChannels();
	}

	@Redirect (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/server/PlayerManager;sendToAll" +
	                                                          "(Lnet/minecraft/text/Text;)V",
	                                                 ordinal = 0))
	private void fukkit_joinmsg(PlayerManager manager, Text text) {
		JOIN_MESSAGES.set(CraftChatMessage.fromComponent(text));
	}

	@Redirect (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/server/PlayerManager;sendToAll" +
	                                                          "(Lnet/minecraft/network/Packet;)V",
	                                                 ordinal = 0))
	private void fukkit_voidSendAll(PlayerManager manager, Packet<?> packet) {
	}

	@Redirect (method = "onPlayerConnect", at = @At (value = "INVOKE", target = "Ljava/util/List;size()I"))
	private int fukkit_overwriteLoop(List list) {
		return 0;
	}

	@Inject (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/PlayerManager;sendToAll" +
	                                                        "(Lnet/minecraft/network/Packet;)V",
	                                               ordinal = 0), cancellable = true,
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_playerJoinEvent(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci,
	                                    GameProfile gameProfile, UserCache userCache, String string,
	                                    CompoundTag compoundTag, ServerWorld serverWorld) {
		String join = JOIN_MESSAGES.get();
		PlayerJoinEvent event = new PlayerJoinEvent(this.craftServer.getPlayer(player), join);
		this.craftServer.getPluginManager().callEvent(event);
		if (!player.networkHandler.connection.isOpen()) {
			ci.cancel();
			return;
		}

		JOIN_MESSAGES.set(join = event.getJoinMessage());
		if (join != null && !join.isEmpty()) {
			for (Text line : CraftChatMessage.fromString(join)) {
				this.server.getPlayerManager().sendToAll(new ChatMessageS2CPacket(line));
			}
		}

		PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player);
		for (int i = 0; i < this.players.size(); i++) {
			ServerPlayerEntity otherPlayer = this.players.get(i);
			if (((CraftPlayer) ((EntityAccess) otherPlayer).getBukkit())
			    .canSee((Player) ((EntityAccess) player).getBukkit())) {
				otherPlayer.networkHandler.sendPacket(packet);
			}

			if (!((Player) ((EntityAccess) player).getBukkit()).canSee((Player) ((EntityAccess) player).getBukkit())) {
				continue;
			}

			player.networkHandler
			.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, otherPlayer));
		}
		((ServerPlayerEntityAccess) player).setHasSentListPacket(true);

		player.networkHandler
		.sendPacket(new EntityTrackerUpdateS2CPacket(player.getEntityId(), player.getDataTracker(), true));

		if (player.world == serverWorld && !serverWorld.getPlayers().contains(player)) {
			serverWorld.onPlayerConnected(player);
			this.server.getBossBarManager().onPlayerConnect(player);
		}
	}

	@Inject (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/world/ServerWorld;onPlayerConnected" +
	                                                        "(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void fukkit_setWorld(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		SERVER_PLAYER_ENTITY.set(player);
	}

	@ModifyVariable (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                                       target = "Lnet/minecraft/server/world/ServerWorld;" +
	                                                                "onPlayerConnected(Lnet/minecraft/server/network" +
	                                                                "/ServerPlayerEntity;)V"),
	                 index = 8, ordinal = 0)
	private ServerWorld fukkit_setWorld(ServerWorld world) {
		ServerPlayerEntity playerEntity = SERVER_PLAYER_ENTITY.get();
		SERVER_PLAYER_ENTITY.set(null);
		return this.server.getWorld(playerEntity.dimension);
	}

	@Redirect (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/server/world/ServerWorld;" +
	                                                          "onPlayerConnected(Lnet/minecraft/server/network" +
	                                                          "/ServerPlayerEntity;)V"))
	private void fukkit_voidCall(ServerWorld world, ServerPlayerEntity player) {

	}

	@Redirect (method = "onPlayerConnect", at = @At (value = "INVOKE",
	                                                 target = "Lnet/minecraft/entity/boss/BossBarManager;" +
	                                                          "onPlayerConnect(Lnet/minecraft/server/network" +
	                                                          "/ServerPlayerEntity;)V"))
	private void fukkit_voidCall0(BossBarManager manager, ServerPlayerEntity player) {

	}

	@Inject (method = "onPlayerConnect", at = @At ("TAIL")) // intentional TAIL
	private void fukkit_connected(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		// fukkit: imperfect copy, not really important though.
		LOGGER.info("{} logged in with entity id {} at {}", player.getName().getString(), player.getEntityId(), player
		                                                                                                        .getBlockPos());
	}

	@Inject (method = "setMainWorld", at = @At ("HEAD"), cancellable = true)
	private void fukkit_saveHandler(ServerWorld world, CallbackInfo ci) {
		if (this.saveHandler != null) { ci.cancel(); }
	}

	@Redirect (method = "setMainWorld", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/world/border/WorldBorder;addListener" +
	                                                       "(Lnet/minecraft/world/border/WorldBorderListener;)V"))
	private void fukkit_newListener(WorldBorder border, WorldBorderListener listener) {
		// TODO verify if it's the same listener
		border.addListener(new CraftWorldBorderListener(((WorldBorderAccess) border).getServerWorld()));
	}

	@Inject (method = "savePlayerData", at = @At ("HEAD"), cancellable = true)
	private void fukkit_nonPersistentPlayers(ServerPlayerEntity player, CallbackInfo ci) {
		if (!((ServerPlayerEntityAccess) player).getBukkit().isPersistent()) { ci.cancel(); }
		SERVER_PLAYER_ENTITY_2.set(player);
	}

	@Redirect (method = "savePlayerData",
	           at = @At (value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
	                     ordinal = 0))
	private Object fukkit_differentStatHandler(Map map, Object key) {
		ServerPlayerEntity entity = SERVER_PLAYER_ENTITY_2.get();
		SERVER_PLAYER_ENTITY_2
		.set(null); // I do not know if ServerStatHandler#save has any unintended side effects that may call this
		// method recursively, I doubt it does but better safe than sorry
		return entity.getStatHandler();
	}

	@Inject (method = "savePlayerData",
	         at = @At (value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
	                   ordinal = 1))
	private void fukkit_getArgs(ServerPlayerEntity player, CallbackInfo ci) {
		SERVER_PLAYER_ENTITY_2.set(player);
	}

	// TODO verify ln 266 is needed

	@Redirect (method = "savePlayerData",
	           at = @At (value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
	                     ordinal = 1))
	private Object fukkit_newPlayerAdvancementTracker(Map map, Object key) {
		ServerPlayerEntity entity = SERVER_PLAYER_ENTITY_2.get();
		SERVER_PLAYER_ENTITY_2.set(null);
		return entity.getAdvancementTracker();
	}

	public String fukkit$disconnect(ServerPlayerEntity entityplayer) { // CraftBukkit - return string
		ServerWorld worldserver = entityplayer.getServerWorld();

		entityplayer.incrementStat(Stats.LEAVE_GAME);

		// CraftBukkit start - Quitting must be before we do final save of data, in case plugins need to modify it
		org.bukkit.craftbukkit.event.CraftEventFactory.handleInventoryCloseEvent(entityplayer);

		PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(this.craftServer
		                                                      .getPlayer(entityplayer), "\u00A7e" + entityplayer
		                                                                                            .getEntityName() + " left the game");
		this.craftServer.getPluginManager().callEvent(playerQuitEvent);
		((ServerPlayerEntityAccess) entityplayer).getBukkit().disconnect(playerQuitEvent.getQuitMessage());

		entityplayer.playerTick();// SPIGOT-924
		// CraftBukkit end

		this.savePlayerData(entityplayer);
		if (entityplayer.hasVehicle()) {
			Entity entity = entityplayer.getRootVehicle();

			if (entity.hasPlayerRider()) {
				LOGGER.debug("Removing player mount");
				entityplayer.stopRiding();
				worldserver.removeEntity(entity);
				Iterator iterator = entity.getPassengersDeep().iterator();

				while (iterator.hasNext()) {
					Entity entity1 = (Entity) iterator.next();

					worldserver.removeEntity(entity1);
				}

				worldserver.getChunk(entityplayer.chunkX, entityplayer.chunkZ).markDirty();
			}
		}

		entityplayer.detach();
		worldserver.removePlayer(entityplayer);
		entityplayer.getAdvancementTracker().clearCriterions();
		this.players.remove(entityplayer);
		this.server.getBossBarManager().onPlayerDisconnenct(entityplayer);
		UUID uuid = entityplayer.getUuid();
		ServerPlayerEntity entityplayer1 = this.playerMap.get(uuid);

		if (entityplayer1 == entityplayer) {
			this.playerMap.remove(uuid);
			// CraftBukkit start
			// this.o.remove(uuid);
			// this.p.remove(uuid);
			// CraftBukkit end
		}

		// CraftBukkit start
		//  this.sendAll(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new
		//  EntityPlayer[]{entityplayer}));
		PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, entityplayer);
		for (int i = 0; i < this.players.size(); i++) {
			ServerPlayerEntity entityplayer2 = this.players.get(i);

			if (((ServerPlayerEntityAccess) entityplayer2).getBukkit().canSee(((ServerPlayerEntityAccess) entityplayer)
			                                                                  .getBukkit())) {
				entityplayer2.networkHandler.sendPacket(packet);
			} else {
				((ServerPlayerEntityAccess) entityplayer2).getBukkit()
				                                          .removeDisconnectingPlayer(((ServerPlayerEntityAccess) entityplayer)
				                                                                     .getBukkit());
			}
		}
		// This removes the scoreboard (and player reference) for the specific player in the manager
		this.craftServer.getScoreboardManager().removePlayer(((ServerPlayerEntityAccess) entityplayer).getBukkit());
		// CraftBukkit end

		return playerQuitEvent.getQuitMessage(); // CraftBukkit
	}

	@Shadow
	protected abstract void savePlayerData(ServerPlayerEntity player);

	// CraftBukkit start - Whole method, SocketAddress to LoginListener, added hostname to signature, return
	// EntityPlayer
	public ServerPlayerEntity fukkit$attemptLogin(ServerLoginNetworkHandler loginlistener, GameProfile gameprofile,
	                                              String hostname) {
		TranslatableText chatmessage;

		// Moved from processLogin
		UUID uuid = PlayerEntity.getUuidFromProfile(gameprofile);
		List<ServerPlayerEntity> list = Lists.newArrayList();

		ServerPlayerEntity entityplayer;

		for (int i = 0; i < this.players.size(); ++i) {
			entityplayer = this.players.get(i);
			if (entityplayer.getUuid().equals(uuid)) {
				list.add(entityplayer);
			}
		}

		Iterator iterator = list.iterator();

		while (iterator.hasNext()) {
			entityplayer = (ServerPlayerEntity) iterator.next();
			this.savePlayerData(entityplayer); // CraftBukkit - Force the player's inventory to be saved
			entityplayer.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.duplicate_login"));
		}

		// Instead of kicking then returning, we need to store the kick reason
		// in the event, check with plugins to see if it's ok, and THEN kick
		// depending on the outcome.
		SocketAddress socketaddress = loginlistener.connection.getAddress();
		ServerPlayerEntity entity = new ServerPlayerEntity(this.server, this.server
		                                                                .getWorld(DimensionType.OVERWORLD),
		gameprofile, new ServerPlayerInteractionManager(this.server
		                                                                                                                                                    .getWorld(DimensionType.OVERWORLD)));
		Player player = ((ServerPlayerEntityAccess) entity).getBukkit();
		PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((java.net.InetSocketAddress) socketaddress)
		                                                                .getAddress());

		if (this.getUserBanList().contains(gameprofile) && !BanEntryUtil
		                                                    .isInvalid(this.getUserBanList().get(gameprofile))) {
			BannedPlayerEntry gameprofilebanentry = this.bannedProfiles.get(gameprofile);

			chatmessage = new TranslatableText("multiplayer.disconnect.banned.reason",
			gameprofilebanentry.getReason());
			if (gameprofilebanentry.getExpiryDate() != null) {
				chatmessage.append(new TranslatableText("multiplayer.disconnect.banned.expiration", DATE_FORMATTER
				                                                                                    .format(gameprofilebanentry
				                                                                                            .getExpiryDate())));
			}

			// return chatmessage;
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
		} else if (!this.isWhitelisted(gameprofile)) {
			chatmessage = new TranslatableText("multiplayer.disconnect.not_whitelisted");
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, CraftChatMessage.fromComponent(chatmessage));
		} else if (this.getIpBanList().isBanned(socketaddress) && !BanEntryUtil
		                                                           .isInvalid(this.getIpBanList().get(socketaddress))) {
			BannedIpEntry ipbanentry = this.bannedIps.get(socketaddress);

			chatmessage = new TranslatableText("multiplayer.disconnect.banned_ip.reason", ipbanentry.getReason());
			if (ipbanentry.getExpiryDate() != null) {
				chatmessage.append(new TranslatableText("multiplayer.disconnect.banned_ip.expiration", DATE_FORMATTER
				                                                                                       .format(ipbanentry
				                                                                                               .getExpiryDate())));
			}

			// return chatmessage;
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
		} else {
			// return this.players.size() >= this.maxPlayers && !this.f(gameprofile) ? new ChatMessage("multiplayer
			// .disconnect.server_full", new Object[0]) : null;
			if (this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(gameprofile)) {
				event.disallow(PlayerLoginEvent.Result.KICK_FULL, "The server is full");
			}
		}

		this.craftServer.getPluginManager().callEvent(event);
		if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {

			((ServerLoginNetworkHandlerAccess) loginlistener).disconnect(event.getKickMessage());
			return null;
		}
		return entity;
	}

	@Shadow
	public abstract BannedPlayerList getUserBanList();

	@Shadow
	public abstract boolean isWhitelisted(GameProfile gameProfile);

	@Shadow
	public abstract BannedIpList getIpBanList();

	@Shadow
	public abstract boolean canBypassPlayerLimit(GameProfile gameProfile);

	public ServerPlayerEntity fukkit$processLogin(GameProfile gameprofile, ServerPlayerEntity player) { // CraftBukkit
		// - added EntityPlayer
		// moved up... :md5pls:
		return player;
	}

	public void fukkit$sendAll(Packet packet, PlayerEntity entityhuman) {
		for (int i = 0; i < this.players.size(); ++i) {
			ServerPlayerEntity entityplayer = this.players.get(i);
			if (entityhuman instanceof ServerPlayerEntity && !((ServerPlayerEntityAccess) entityplayer).getBukkit()
			                                                                                           .canSee(((ServerPlayerEntityAccess) entityhuman)
			                                                                                                   .getBukkit())) {
				continue;
			}
			this.players.get(i).networkHandler.sendPacket(packet);
		}
	}

	public void fukkit$sendAll(Packet<?> packet, World world) {
		for (int i = 0; i < world.getPlayers().size(); ++i) {
			((ServerPlayerEntity) world.getPlayers().get(i)).networkHandler.sendPacket(packet);
		}
	}

	public void fukkit$sendMessage(Text[] lines) {
		for (Text line : lines) {
			this.broadcastChatMessage(line, true);
		}
	}

	@Shadow public abstract void broadcastChatMessage(Text text, boolean system);

	/**
	 * public ServerPlayerEntity respawnPlayer(ServerPlayerEntity entityplayer, DimensionType dimensionmanager,
	 * boolean flag) {
	 * return this.fukkit$moveToWorld(entityplayer, dimensionmanager, flag, null, true);
	 * }
	 */

	// TODO fix
	/*public ServerPlayerEntity fukkit$moveToWorld(ServerPlayerEntity playerEntity, DimensionType dimensionType,
	boolean alive, Location location, boolean avoidSuffocation) {
		playerEntity.stopRiding(); // CraftBukkit
		this.players.remove(playerEntity);
		playerEntity.getServerWorld().removePlayer(playerEntity);
		BlockPos blockposition = playerEntity.getSpawnPosition();
		boolean flag1 = playerEntity.isSpawnForced();
		ServerPlayerEntity newPlayer = playerEntity;
		org.bukkit.World fromWorld = ((EntityAccess) playerEntity).getBukkit().getWorld();
		playerEntity.notInAnyWorld = false;
		// CraftBukkit end

		// md5 pls wth m8
		newPlayer.networkHandler = playerEntity.networkHandler;
		newPlayer.copyFrom(playerEntity, alive);
		newPlayer.setEntityId(playerEntity.getEntityId());
		newPlayer.setMainArm(playerEntity.getMainArm());
		for (String s : playerEntity.getScoreboardTags()) {
			newPlayer.addScoreboardTag(s);
		}

		// WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);  // CraftBukkit - handled
		later

		// this.a(entityplayer1, entityplayer, worldserver); // CraftBukkit - removed

		// CraftBukkit start - fire PlayerRespawnEvent
		if (location == null) {
			boolean isBedSpawn = false;
			CraftWorld craftWorld = (CraftWorld) ((MinecraftServerAccess) this.server).getBukkit().getWorld
			(playerEntity.spawnWorld);
			if (craftWorld != null && blockposition != null) {
				Optional<Vec3d> optional = PlayerEntity.getBed(craftWorld.getHandle(), blockposition, flag1);

				if (optional.isPresent()) {
					Vec3d vec3d = optional.get();

					isBedSpawn = true;
					location = new Location(craftWorld, vec3d.x, vec3d.y, vec3d.z);
				} else {
					newPlayer.setPlayerSpawn(null, true, false);
					newPlayer.networkHandler.sendPacket(new GameStateChangeS2CPacket(0, 0.0F));
				}
			}

			if (location == null) {
				craftWorld = (CraftWorld) ((MinecraftServerAccess) this.server).getBukkit().getWorlds().get(0);
				blockposition = newPlayer.getSpawnPoint(craftWorld.getHandle());
				location = new Location(craftWorld, (float) blockposition.getX() + 0.5F, (float) blockposition.getY()
				+ 0.1F, (float) blockposition.getZ() + 0.5F);
			}

			Player respawnPlayer = this.craftServer.getPlayer(newPlayer);
			PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn);
			this.craftServer.getPluginManager().callEvent(respawnEvent);

			location = respawnEvent.getRespawnLocation();
			if (!alive) playerEntity.reset(); // SPIGOT-4785
		} else {
			location.setWorld(((WorldAccess) this.server.getWorld(dimensionType)).getBukkit());
		}
		ServerWorld serverWorld = ((CraftWorld) location.getWorld()).getHandle();
		((ServerPlayerEntityAccess)newPlayer).forceSetPositionRotation(location.getX(), location.getY(), location.getZ
		(), location.getYaw(), location.getPitch());
		// CraftBukkit end

		while (avoidSuffocation && !serverWorld.doesNotCollide(newPlayer) && newPlayer.getY() < 256.0D) {
			newPlayer.setPos(newPlayer.getX(), newPlayer.getY() + 1.0D, newPlayer.getZ());
		}
		// CraftBukkit start
		// Force the client to refresh their chunk cache
		if (fromWorld.getEnvironment() == ((WorldAccess) serverWorld).getBukkit().getEnvironment()) {
			newPlayer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverWorld.dimension.getType().getRawId()
			>= 0 ? DimensionType.THE_NETHER : DimensionType.OVERWORLD, LevelProperties.c(serverWorld
			.getLevelProperties().getSeed()), serverWorld.getLevelProperties().getGeneratorType(), playerEntity
			.interactionManager.getGameMode()));
		}

		LevelProperties worlddata = serverWorld.getLevelProperties();

		newPlayer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(((DimensionTypeAccess) serverWorld.dimension
		.getType()).getType(), LevelProperties.c(serverWorld.getLevelProperties().getSeed()), serverWorld
		.getLevelProperties().getGeneratorType(), newPlayer.interactionManager.getGameMode()));
		newPlayer.setWorld(serverWorld);
		newPlayer.removed = false;
		newPlayer.networkHandler.teleport(new Location(((WorldAccess) serverWorld).getBukkit(), newPlayer.getX(),
		newPlayer.getY(), newPlayer.getZ(), newPlayer.yaw, newPlayer.pitch));
		newPlayer.setSneaking(false);
		BlockPos spawnPos = serverWorld.getSpawnPos();

		// entityplayer1.playerConnection.a(entityplayer1.locX(), entityplayer1.locY(), entityplayer1.locZ(),
		entityplayer1.yaw, entityplayer1.pitch);
		newPlayer.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(spawnPos));
		newPlayer.networkHandler.sendPacket(new DifficultyS2CPacket(worlddata.getDifficulty(), worlddata
		.isDifficultyLocked()));
		newPlayer.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(newPlayer.experienceProgress, newPlayer
		.totalExperience, newPlayer.experienceLevel));
		this.sendWorldInfo(newPlayer, serverWorld);
		this.sendCommandTree(newPlayer);

		if (!playerEntity.networkHandler.isDisconnected()) {
			serverWorld.onPlayerRespawned(newPlayer);
			this.players.add(newPlayer);
			this.playerMap.put(newPlayer.getUuid(), newPlayer);
		}
		// entityplayer1.syncInventory();
		newPlayer.setHealth(newPlayer.getHealth());
		// Added from changeDimension
		this.method_14594(playerEntity); // Update health, etc...
		playerEntity.sendAbilitiesUpdate();
		for (Object o1 : playerEntity.getStatusEffects()) {
			StatusEffectInstance mobEffect = (StatusEffectInstance) o1;
			playerEntity.networkHandler.sendPacket(new EntityPotionEffectS2CPacket(playerEntity.getEntityId(),
			mobEffect));
		}

		// Fire advancement trigger
		((ServerPlayerEntityAccess) playerEntity).dimensionChanged(((CraftWorld) fromWorld).getHandle());

		// Don't fire on respawn
		if (fromWorld != location.getWorld()) {
			PlayerChangedWorldEvent event = new PlayerChangedWorldEvent((Player) ((EntityAccess) playerEntity)
			.getBukkit(), fromWorld);
			((MinecraftServerAccess) this.server).getBukkit().getPluginManager().callEvent(event);
		}

		// Save player file again if they were disconnected
		if (playerEntity.networkHandler.isDisconnected()) {
			this.savePlayerData(playerEntity);
		}
		// CraftBukkit end
		return newPlayer;
	}*/
	@Redirect (method = "updatePlayerLatency", at = @At (value = "INVOKE",
	                                                     target = "Lnet/minecraft/server/PlayerManager;sendToAll" +
	                                                              "(Lnet/minecraft/network/Packet;)V"))
	private void fukkit_invisOrSomething(PlayerManager manager, Packet<?> packet) {
		for (int i = 0; i < this.players.size(); i++) {
			final ServerPlayerEntity target = this.players.get(0);
			target.networkHandler
			.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LATENCY, this.players.stream()
			                                                                                           .filter(e -> ((ServerPlayerEntityAccess) target)
			                                                                                                        .getBukkit()
			                                                                                                        .canSee(((ServerPlayerEntityAccess) e)
			                                                                                                                .getBukkit()))
			                                                                                           .collect(Collectors
			                                                                                                    .toList())));
		}
	}

	@Inject (method = "sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;I)V",
	         at = @At (value = "INVOKE",
	                   target = "Lnet/minecraft/server/command/CommandManager;sendCommandTree" +
	                            "(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void fukkit_recalcPerms(ServerPlayerEntity player, int permissionLevel, CallbackInfo ci) {
		((ServerPlayerEntityAccess) player).getBukkit().recalculatePermissions();
	}

	@Inject (method = "sendToAround", at = @At (value = "JUMP", ordinal = 0, opcode = Opcodes.IF_ACMPNE),
	         cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_verifySight(PlayerEntity player, double x, double y, double z, double d,
	                                DimensionType dimension, Packet<?> packet, CallbackInfo ci, int i,
	                                ServerPlayerEntity otherPlayer) {
		if (player instanceof ServerPlayerEntity && !((ServerPlayerEntityAccess) player).getBukkit()
		                                                                                .canSee(((ServerPlayerEntityAccess) otherPlayer)
		                                                                                        .getBukkit())) {
			ci.cancel();
		}
	}

	@ModifyVariable (method = "sendWorldInfo", at = @At (value = "INVOKE",
	                                                     target = "Lnet/minecraft/server/network" +
	                                                              "/ServerPlayNetworkHandler;sendPacket" +
	                                                              "(Lnet/minecraft/network/Packet;)V",
	                                                     ordinal = 0))
	private WorldBorder fukkit_diffBorder(WorldBorder border, ServerPlayerEntity player, ServerWorld world) {
		return player.world.getWorldBorder();
	}

	@Inject (method = "sendWorldInfo", at = @At (value = "INVOKE",
	                                             target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;" +
	                                                      "sendPacket(Lnet/minecraft/network/Packet;)V",
	                                             ordinal = 5, shift = At.Shift.AFTER))
	private void fukkit_handlePlayerWeather(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
		((ServerPlayerEntityAccess) player).setPlayerWeather(WeatherType.DOWNFALL, false);
		float rainGrad = world.getRainGradient(1f);
		float thunderGrad = world.getThunderGradient(1f) / rainGrad;
		((ServerPlayerEntityAccess) player).updateWeather(-rainGrad, rainGrad, -thunderGrad, thunderGrad);
	}

	@Redirect (method = "sendWorldInfo", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
	                                               ordinal = 3))
	private void fukkit_handlePlayerWeather0(ServerPlayNetworkHandler handler, Packet<?> packet) {

	}

	@Redirect (method = "sendWorldInfo", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
	                                               ordinal = 4))
	private void fukkit_handlePlayerWeather1(ServerPlayNetworkHandler handler, Packet<?> packet) {

	}

	@Redirect (method = "sendWorldInfo", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
	                                               ordinal = 5))
	private void fukkit_handlePlayerWeather2(ServerPlayNetworkHandler handler, Packet<?> packet) {

	}

	@Redirect (method = "method_14594", at = @At (value = "INVOKE",
	                                              target = "Lnet/minecraft/server/network/ServerPlayerEntity;markHealthDirty()V"))
	private void fukkit_updateScaledHealth(ServerPlayerEntity entity) {
		((ServerPlayerEntityAccess) entity).getBukkit().updateScaledHealth();
	}

	@Inject (method = "method_14594", at = @At ("TAIL"))
	private void fukkit_gameRules(ServerPlayerEntity player, CallbackInfo ci) {
		byte debugInfo = (byte) (player.world.getGameRules().getBoolean(GameRules.REDUCED_DEBUG_INFO) ? 22 : 23);
		player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, debugInfo));
	}

	@Redirect (method = "disconnectAllPlayers", at = @At (value = "INVOKE",
	                                                      target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
	private void fukkit_customShutdownMsg(ServerPlayNetworkHandler handler, Text text) {
		//handler.disconnect(((MinecraftServerAccess)server).getBukkit().getShutdownMessage());
	}

	@ModifyArg (method = "broadcastChatMessage", at = @At (value = "INVOKE",
	                                                       target = "Lnet/minecraft/network/packet/s2c/play/ChatMessageS2CPacket;<init>(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;)V"),
	            index = 0)
	private Text fukkit_sendToAll(Text text) {
		return CraftChatMessage.fixComponent(text);
	}

	@ModifyVariable (method = "createStatHandler", at = @At (value = "JUMP", ordinal = 1, opcode = Opcodes.IFNONNULL))
	private ServerStatHandler fukkit_createStatHandler(ServerStatHandler statHandler, PlayerEntity player) {
		return ((ServerPlayerEntity) player).getStatHandler();
	}

	@Redirect (method = "createStatHandler", at = @At (value = "INVOKE",
	                                                   target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private <K, V> V fukkit_voidCall(Map map, K key, V value) {
		return null;
	}

	@Redirect (method = "onDataPacksReloaded",
	           at = @At (value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;",
	                     ordinal = 0))
	private Iterator fukkit_iterator(Collection collection) {
		return Collections.emptyIterator(); // hahayes
	}

	@Inject (method = "onDataPacksReloaded", at = @At (value = "INVOKE",
	                                                   target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
	private void fukkit_flush(CallbackInfo ci) {
		for (ServerPlayerEntity player : this.players) {
			player.getAdvancementTracker().reload();
			player.getAdvancementTracker().sendUpdate(player);
		}
	}
}
