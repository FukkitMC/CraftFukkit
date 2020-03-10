package com.github.fukkitmc.fukkit.mixin.net.minecraft.server;

import com.github.fukkitmc.fukkit.FukkitInit;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.MinecraftServerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.ServerNetworkIoAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.server.world.ThreadedAnvilChunkStorageAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.level.LevelPropertiesAccess;
import com.github.fukkitmc.fukkit.util.Constructors;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import joptsimple.OptionSet;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.server.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Unit;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.DisableableProfiler;
import net.minecraft.util.snooper.SnooperListener;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.*;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.event.server.ServerLoadEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import static org.bukkit.Bukkit.getAllowNether;

@Implements (@Interface (iface = MinecraftServerAccess.class, prefix = "fukkit$"))
@Mixin (MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask> implements SnooperListener,
                                                                                                  CommandOutput,
                                                                                                  AutoCloseable,
                                                                                                  Runnable {
	@Final
	@Shadow
	private static final Logger LOGGER = null;
	private static int currentTick = (int) (System.currentTimeMillis() / 50);
	public org.bukkit.craftbukkit.CraftServer server;
	public OptionSet options;
	public org.bukkit.command.ConsoleCommandSender console;
	public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
	public ConsoleReader reader;
	public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<Runnable>();
	public int autosavePeriod;
	public File bukkitDataPackFolder;
	public CommandManager vanillaCommandDispatcher;
	@Shadow
	@Final
	protected WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;
	@Mutable
	@Shadow
	@Final
	private Map<DimensionType, ServerWorld> worlds;
	@Shadow
	@Final
	private CommandManager commandManager;
	@Shadow
	@Final
	private File gameDir;
	@Mutable
	@Shadow
	@Final
	private LevelStorage levelStorage;
	@Shadow
	@Final
	private DataFixer dataFixer;
	@Shadow
	@Final
	private Executor workerExecutor;
	@Shadow
	@Final
	private DisableableProfiler profiler;
	@Shadow
	private DataCommandStorage dataCommandStorage;
	@Shadow
	private PlayerManager playerManager;
	@Shadow
	private long timeReference;
	@Shadow
	@Final
	private ServerNetworkIo networkIo;
	private boolean forceTicks;

	public MinecraftServerMixin(String name) {
		super(name);
	}

	@Inject (method = "<init>", at = @At ("TAIL"))
	public void postInit(File gameDir, Proxy proxy, DataFixer dataFixer, CommandManager commandManager,
	                     YggdrasilAuthenticationService authService, MinecraftSessionService sessionService,
	                     GameProfileRepository gameProfileRepository, UserCache userCache,
	                     WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory,
	                     String levelName, CallbackInfo ci) {
		this.levelStorage = null;
		this.worlds = Maps.newLinkedHashMap();
		this.vanillaCommandDispatcher = commandManager;
		if (System.console() == null && System.getProperty("jline.terminal") == null) {
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
			FukkitInit.useJLine = false;
		}

		try {
			//Class.forName("org.fusesource.jansi.WindowsAnsiOutputStream");
			this.reader = new ConsoleReader(System.in, System.out);
			this.reader.setExpandEvents(false); // Avoid parsing exceptions for uncommonly used event designators
		} catch (Throwable e) {
			try {
				// Try again with jline disabled for Windows users without C++ 2008 Redistributable
				System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
				System.setProperty("user.language", "en");
				Main.useJline = false;
				this.reader = new ConsoleReader(System.in, System.out);
				this.reader.setExpandEvents(false);
			} catch (IOException ex) {
				FukkitInit.LOGGER.warn(ex);
				throw new RuntimeException(ex);
			}
		}
		Runtime.getRuntime()
		       .addShutdownHook(new org.bukkit.craftbukkit.util.ServerShutdownThread((MinecraftServer) (Object) this));
	}

	@Redirect (method = "upgradeWorld(Ljava/lang/String;)V",
	           at = @At (target = "Lnet/minecraft/server/MinecraftServer;getLevelName()Ljava/lang/String;",
	                     value = "INVOKE"))
	public String getLevelname(MinecraftServer server, String s) { // killing two birds with one
		return s; // craftbukkit thonk
	}

	/**
	 * thank you craftbukkit, very cool!
	 *
	 * @author HalfOf2
	 */
	@Overwrite
	public void loadWorld(String s, String serverName, long seed, LevelGeneratorType generatorType,
	                      JsonElement generatorSettings) throws IllegalAccessException, InvocationTargetException,
	                                                            InstantiationException {
		// this.convertWorld(s); // CraftBukkit - moved down
		this.setLoadingStage(new TranslatableText("menu.loadingLevel"));
        /* CraftBukkit start - Remove ticktime arrays and worldsettings
        WorldNBTStorage worldnbtstorage = this.getConvertable().a(s, this);

        this.a(this.getWorld(), worldnbtstorage);
        WorldData worlddata = worldnbtstorage.getWorldData();
        WorldSettings worldsettings;

        if (worlddata == null) {
            if (this.isDemoMode()) {
                worldsettings = MinecraftServer.c;
            } else {
                worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this
                .isHardcore(), worldtype);
                worldsettings.setGeneratorSettings(jsonelement);
                if (this.bonusChest) {
                    worldsettings.a();
                }
            }

            worlddata = new WorldData(worldsettings, s1);
        } else {
            worlddata.setName(s1);
            worldsettings = new WorldSettings(worlddata);
        }

        this.a(worldnbtstorage.getDirectory(), worlddata);
        */
		int worldCount = 3;

		for (int j = 0; j < worldCount; ++j) {
			ServerWorld world;
			LevelProperties worlddata;
			byte dimension = 0;

			if (j == 1) {
				if (getAllowNether()) {
					dimension = -1;
				} else {
					continue;
				}
			}

			if (j == 2) {
				if (this.server.getAllowEnd()) {
					dimension = 1;
				} else {
					continue;
				}
			}

			String worldType = Objects.requireNonNull(World.Environment.getEnvironment(dimension)).toString()
			                          .toLowerCase();
			String name = (dimension == 0) ? s : s + "_" + worldType;
			//this.convertWorld(name); // Run conversion now
			this.upgradeWorld(name);
			org.bukkit.generator.ChunkGenerator gen = this.server.getGenerator(name);
			LevelInfo worldsettings = new LevelInfo(seed, this.getDefaultGameMode(), this
			                                                                         .shouldGenerateStructures(), this
			                                                                                                      .isHardcore(), Constructors
			                                                                                                                     .newLevelGeneratorType(dimension, name));
			worldsettings.setGeneratorOptions(generatorSettings);

			if (j == 0) {
				WorldSaveHandler worldnbtstorage = new WorldSaveHandler(this.server
				                                                        .getWorldContainer(), serverName,
				(MinecraftServer) (Object) this, this.dataFixer);
				worlddata = worldnbtstorage.readProperties();
				if (worlddata == null) {
					worlddata = new LevelProperties(worldsettings, serverName);
				}
				((LevelPropertiesAccess) worlddata)
				.checkName(serverName); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to
				// take the last loaded world as respawn (in this case the end)
				this.loadWorldDataPacks(worldnbtstorage.getWorldDir(), worlddata);
				WorldGenerationProgressListener worldloadlistener = this.worldGenerationProgressListenerFactory
				                                                    .create(11);

				if (this.isDemo()) {
					worlddata.loadLevelInfo(MinecraftServer.DEMO_LEVEL_INFO);
				}
				world = Constructors
				        .newWorld((MinecraftServer) (Object) this, this.workerExecutor, worldnbtstorage, worlddata,
				        DimensionType.OVERWORLD, this.profiler, worldloadlistener, gen, World.Environment
				                                                                        .getEnvironment(dimension));

				PersistentStateManager worldpersistentdata = world.getPersistentStateManager();
				this.initScoreboard(worldpersistentdata);
				this.server.scoreboardManager =
				new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager((MinecraftServer) (Object) this, world
				                                                                                              .getScoreboard());
				this.dataCommandStorage = new DataCommandStorage(worldpersistentdata);
			} else {
				String dim = "DIM" + dimension;

				File newWorld = new File(new File(name), dim);
				File oldWorld = new File(new File(s), dim);
				File oldLevelDat = new File(new File(s), "level.dat"); // The data folders exist on first run as they
				// are created in the PersistentCollection constructor above, but the level.dat won't

				if (!newWorld.isDirectory() && oldWorld.isDirectory() && oldLevelDat.isFile()) {

					LOGGER.info("---- Migration of old " + worldType + " folder required ----");
					LOGGER
					.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit" +
					      " " +
					      "requires that you move your " + worldType + " folder to a new location in order to operate" +
					      " " +
					      "correctly.");
					LOGGER
					.info("We will move this folder for you, but it will mean that you need to move it back should " +
					      "you" +
					      " wish to stop using Bukkit in the future.");
					LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

					if (newWorld.exists()) {
						LOGGER.warn("A file or folder already exists at " + newWorld + "!");
						LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
					} else if (newWorld.getParentFile().mkdirs()) {
						if (oldWorld.renameTo(newWorld)) {
							LOGGER
							.info("Success! To restore " + worldType + " in the future, simply move " + newWorld +
							      " " +
							      "to " + oldWorld);
							// Migrate world data too.
							try {
								com.google.common.io.Files.copy(oldLevelDat, new File(new File(name), "level.dat"));
								org.apache.commons.io.FileUtils
								.copyDirectory(new File(new File(s), "data"), new File(new File(name), "data"));
							} catch (IOException exception) {
								LOGGER.warn("Unable to migrate world data.");
							}
							LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
						} else {
							LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
							LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
						}
					} else {
						LOGGER.warn("Could not create path for " + newWorld + "!");
						LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
					}
				}

				WorldSaveHandler worldnbtstorage = new WorldSaveHandler(this.server
				                                                        .getWorldContainer(), name,
				(MinecraftServer) (Object) this, this.dataFixer);
				// world =, b0 to dimension, s1 to name, added Environment and gen
				worlddata = worldnbtstorage.readProperties();
				if (worlddata == null) {
					worlddata = new LevelProperties(worldsettings, name);
				}

				((LevelPropertiesAccess) worlddata)
				.checkName(name);// CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the
				// last loaded world as respawn (in this case the end)
				WorldGenerationProgressListener worldloadlistener = this.worldGenerationProgressListenerFactory
				                                                    .create(11);
				world = Constructors.newWorld2nd(this
				                                 .getWorld(DimensionType.OVERWORLD), (MinecraftServer) (Object) this,
				this.workerExecutor, worldnbtstorage, DimensionType
				                                      .byRawId(dimension), this.profiler, worldloadlistener, gen,
				World.Environment
				                                                                                                  .getEnvironment(dimension));
			}


			this.initWorld(world, worlddata, worldsettings);
			this.server.getPluginManager()
			           .callEvent(new org.bukkit.event.world.WorldInitEvent(((WorldAccess) world).getBukkit()));
			this.worlds.put(world.getDimension().getType(), world);
			this.playerManager.setMainWorld(world);


			if (worlddata.getCustomBossEvents() != null) {
				this.getBossBarManager().fromTag(worlddata.getCustomBossEvents());
			}
		}
		this.setDifficulty(this.getDefaultDifficulty(), true);
		for (ServerWorld worldserver : this.getWorlds()) {
			this.loadSpawn(((ThreadedAnvilChunkStorageAccess) worldserver.getChunkManager().threadedAnvilChunkStorage)
			               .getListener(), worldserver);
			this.server.getPluginManager()
			           .callEvent(new org.bukkit.event.world.WorldLoadEvent(((WorldAccess) worldserver).getBukkit()));
		}

		this.server.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
		this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
		((ServerNetworkIoAccess) this.networkIo).acceptConnections();
		// CraftBukkit end
	}

	@Shadow
	protected abstract void setLoadingStage(Text loadingStage);

	@Shadow
	protected abstract void upgradeWorld(String string);

	@Shadow
	public abstract GameMode getDefaultGameMode();

	@Shadow
	public abstract boolean shouldGenerateStructures();

	@Shadow
	public abstract boolean isHardcore();

	@Shadow
	protected abstract void loadWorldDataPacks(File worldDir, LevelProperties levelProperties);

	@Shadow
	public abstract boolean isDemo();

	@Shadow
	protected abstract void initScoreboard(PersistentStateManager persistentStateManager);

	@Shadow
	public abstract ServerWorld getWorld(DimensionType dimensionType);

	public void initWorld(ServerWorld worldserver1, LevelProperties worlddata, LevelInfo worldsettings) {
		worldserver1.getWorldBorder().load(worlddata);// TODO verify this is infact the right method, and it's not save

		// CraftBukkit start
		if (((WorldAccess) worldserver1).getGenerator() != null) {
			((WorldAccess) worldserver1).getBukkit().getPopulators().addAll(((WorldAccess) worldserver1).getGenerator()
			                                                                                            .getDefaultPopulators(((WorldAccess) worldserver1)
			                                                                                                                  .getBukkit()));
		}
		// CraftBukkit end

		if (!worlddata.isInitialized()) {
			try {

				worldserver1.init(worldsettings);
				if (worlddata.getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
					this.setToDebugWorldProperties(worlddata);
				}

				worlddata.setInitialized(true);
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.create(throwable, "Exception initializing level");

				try {
					worldserver1.addDetailsToCrashReport(crashreport);
				} catch (Throwable throwable1) {
				}

				throw new CrashException(crashreport);
			}

			worlddata.setInitialized(true); // TODO verify
		}
	}

	@Shadow
	public abstract BossBarManager getBossBarManager();

	@Shadow
	public abstract void setDifficulty(Difficulty difficulty, boolean bl);

	@Shadow
	public abstract Difficulty getDefaultDifficulty();

	@Shadow
	public abstract Iterable<ServerWorld> getWorlds();

	// CraftBukkit start
	public void loadSpawn(WorldGenerationProgressListener worldloadlistener, ServerWorld worldserver) {
		if (!((WorldAccess) worldserver.getWorld()).keepSpawnInMemory()) {
			return;
		}

		this.setLoadingStage(new TranslatableText("menu.generatingTerrain"));
		// WorldServer worldserver = this.getWorldServer(DimensionManager.OVERWORLD);
		this.forceTicks = true;
		// CraftBukkit end

		//LOGGER.info("Preparing start region for dimension '{}'/{}", worldserver.getLevelProperties().getLevelName(),
		// DimensionType.a(worldserver.dimension.getType().getType())); // CraftBukkit
		LOGGER.info("Preparing start region for dimension '{}'", worldserver.getLevelProperties()
		                                                                    .getLevelName()); // suck my dick
		// craftbukkit

		BlockPos blockposition = worldserver.getSpawnPos();

		worldloadlistener.start(new ChunkPos(blockposition));
		ServerChunkManager chunkproviderserver = worldserver.getChunkManager();

		chunkproviderserver.getLightingProvider().setTaskBatchSize(500);
		this.timeReference = Util.getMeasuringTimeMs();
		chunkproviderserver.addTicket(ChunkTicketType.START, new ChunkPos(blockposition), 11, Unit.INSTANCE);

		while (chunkproviderserver.getTotalChunksLoadedCount() != 441) {
			// CraftBukkit start
			// this.nextTick = SystemUtils.getMonotonicMillis() + 10L;
			this.executeModerately();
			// CraftBukkit end
		}

		// CraftBukkit start
		// this.nextTick = SystemUtils.getMonotonicMillis() + 10L;
		this.executeModerately();
		// Iterator iterator = DimensionManager.a().iterator();

		if (true) {
			DimensionType dimensionmanager = worldserver.dimension.getType();
			ForcedChunkState forcedchunk = worldserver.getPersistentStateManager().get(ForcedChunkState::new,
			"chunks");
			// CraftBukkit end

			if (forcedchunk != null) {
				ServerWorld worldserver1 = this.getWorld(dimensionmanager);
				LongIterator longiterator = forcedchunk.getChunks().iterator();
				while (longiterator.hasNext()) {
					long i = longiterator.nextLong();
					ChunkPos chunkcoordintpair = new ChunkPos(i);

					worldserver1.getChunkManager().setChunkForced(chunkcoordintpair, true);
				}
			}
		}

		// CraftBukkit start
		// this.nextTick = SystemUtils.getMonotonicMillis() + 10L;
		this.executeModerately();
		// CraftBukkit end
		worldloadlistener.stop();
		chunkproviderserver.getLightingProvider().setTaskBatchSize(5);

		// CraftBukkit start
		this.forceTicks = false;
		// CraftBukkit end
	}

	@Shadow
	protected abstract void setToDebugWorldProperties(LevelProperties properties);

	private void executeModerately() {
		this.runTasks();
		java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
	}

	/**
	 * @author HalfOf2
	 */
	@Overwrite
	public void prepareStartRegion(WorldGenerationProgressListener worldGenerationProgressListener) {
		throw new UnsupportedOperationException("Unsupported, sorry, craftbukkit said so :(");
	}

	public Object fukkit$getBukkit() {
		return this.server;
	}

	public void fukkit$setBukkit(Object object) {
		this.server = (CraftServer) object;
	}

	public ConsoleCommandSender fukkit$getConsoleCommandSender() {
		return this.console;
	}

	public void fukkit$setConsoleCommandSender(ConsoleCommandSender console) {
		this.console = console;
	}

	public ConsoleReader fukkit$getReader() {
		return this.reader;
	}
}
