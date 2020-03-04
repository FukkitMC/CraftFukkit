package com.github.fukkitmc.fukkit.util;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.LecternContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.HungerManagerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.dimension.DimensionTypeAccess;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ComposterBlock;
import net.minecraft.container.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.SecondaryServerWorld;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.source.BiomeAccessType;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.inventory.CraftBlockInventoryHolder;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.InventoryHolder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

public class Constructors {
	private static final Constructor<LevelGeneratorType> LEVELGENERATORTYPE_I_STRING;
	private static final Constructor<DimensionType> DIMENSION_TYPE_CONSTRUCTOR;
	private static final Constructor<BasicInventory> COMPOSTER_BLOCK_DUMMY_INVENTORY_CONSTRUCTOR;
	static {
		try {
			LEVELGENERATORTYPE_I_STRING = LevelGeneratorType.class.getDeclaredConstructor(int.class, String.class);
			DIMENSION_TYPE_CONSTRUCTOR = DimensionType.class.getDeclaredConstructor(int.class, String.class, String.class, BiFunction.class, boolean.class, BiomeAccessType.class);
			COMPOSTER_BLOCK_DUMMY_INVENTORY_CONSTRUCTOR = findInnerClassConstructor(ComposterBlock.class, "DummyInventory");
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> Constructor<T> findInnerClassConstructor(Class<?> declaration, String match, Class<?>...args) throws NoSuchMethodException {
		for (Class<?> innerClass : declaration.getDeclaredClasses()) {
			if(innerClass.getName().contains(match)) { // ohyes
				return (Constructor<T>) innerClass.getConstructor(args);
			}
		}
		return null;
	}

	public static LevelGeneratorType newLevelGeneratorType(int id, String name) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return LEVELGENERATORTYPE_I_STRING.newInstance(id, name);
	}

	public static DimensionType newDimensionType(int i, String s, String s1, BiFunction<World, DimensionType, ? extends Dimension> bifunction, boolean flag, BiomeAccessType genlayerzoomer, DimensionType type) {
		try {
			DimensionType type1 = DIMENSION_TYPE_CONSTRUCTOR.newInstance(i, s, s1, bifunction, flag, genlayerzoomer);
			((DimensionTypeAccess)type1).setType(type);
			return type1;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	// consider using an Unsafe instance instead, not sure if it'll be better or worse
	public static ServerWorld newWorld(MinecraftServer server, Executor workerExecutor, WorldSaveHandler worldSaveHandler, LevelProperties properties, DimensionType dimensionType, Profiler profiler, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator generator, World.Environment environment) {
		ServerWorld world = new ServerWorld(server, workerExecutor, worldSaveHandler, properties, dimensionType, profiler, worldGenerationProgressListener);
		WorldAccess access = (WorldAccess) world;
		access.setGenerator(generator);
		access.setBukkit(new CraftWorld(world, generator, environment));
		access.setTicksPerAnimalSpawns(access.getBukkitServer().getTicksPerAnimalSpawns());
		access.setTicksSpawnMonsterSpawns(access.getBukkitServer().getTicksPerMonsterSpawns());
		net.minecraft.world.gen.chunk.ChunkGenerator<?> gen;
		if(generator != null) {
			gen = new CustomChunkGenerator(world, generator);
			access.setChunkManager(new ServerChunkManager(world, worldSaveHandler.getWorldDir(), worldSaveHandler.getDataFixer(), worldSaveHandler.getStructureManager(), workerExecutor, gen, server.getPlayerManager().getViewDistance(), worldGenerationProgressListener, () -> server.getWorld(DimensionType.OVERWORLD).getPersistentStateManager()));
		}
		return world;
	}

	public static SecondaryServerWorld newWorld2nd(ServerWorld sworld, MinecraftServer server, Executor workerExecutor, WorldSaveHandler worldSaveHandler, DimensionType dimensionType, Profiler profiler, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator generator, World.Environment environment) {
		SecondaryServerWorld world = new SecondaryServerWorld(sworld, server, workerExecutor, worldSaveHandler, dimensionType, profiler, worldGenerationProgressListener);
		WorldAccess access = (WorldAccess) world;
		access.setGenerator(generator);
		access.setBukkit(new CraftWorld(world, generator, environment));
		access.setTicksPerAnimalSpawns(access.getBukkitServer().getTicksPerAnimalSpawns());
		access.setTicksSpawnMonsterSpawns(access.getBukkitServer().getTicksPerMonsterSpawns());
		net.minecraft.world.gen.chunk.ChunkGenerator<?> gen;
		if(generator != null) {
			gen = new CustomChunkGenerator(world, generator);
			access.setChunkManager(new ServerChunkManager(world, worldSaveHandler.getWorldDir(), worldSaveHandler.getDataFixer(), worldSaveHandler.getStructureManager(), workerExecutor, gen, server.getPlayerManager().getViewDistance(), worldGenerationProgressListener, () -> server.getWorld(DimensionType.OVERWORLD).getPersistentStateManager()));
		}
		return world;
	}

	public static BasicInventory newBasicInventory(int size, InventoryHolder holder) {
		BasicInventory inventory = new BasicInventory(size);
		((InventoryAccess)inventory).setOwner(holder);
		return inventory;
	}

	public static CraftingInventory newCraftingInventory(Container container, int width, int height, PlayerEntity entity) {
		CraftingInventory inventory = new CraftingInventory(container, width, height);
		((InventoryAccess)inventory).setOwner((InventoryHolder) ((EntityAccess)entity).getBukkit());
		return inventory;
	}

	public static BasicInventory newComposterBlock$DummyInventory(IWorld world, BlockPos pos) {
		try {
			BasicInventory dummy = COMPOSTER_BLOCK_DUMMY_INVENTORY_CONSTRUCTOR.newInstance();
			((InventoryAccess)dummy).setOwner(new CraftBlockInventoryHolder(world, pos, dummy));
			return dummy;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static EnderChestInventory newEnderChestInventory(PlayerEntity owner) {
		EnderChestInventory inventory = new EnderChestInventory();
		((InventoryAccess)inventory).setOwner(((PlayerEntityAccess<?>)owner).getBukkit());
		return inventory;
	}

	public static HungerManager newHungerManager(PlayerEntity entity) {
		HungerManager manager = new HungerManager();
		((HungerManagerAccess)manager).setPlayer(entity);
		return manager;
	}

	public static LecternContainer newLecternContainer(int syncId, PlayerInventory playerInventory) {
		LecternContainer container = new LecternContainer(syncId);
		((LecternContainerAccess)container).setPlayer(((PlayerEntityAccess<CraftPlayer>)playerInventory.player).getBukkit());
		return container;
	}

	public static LecternContainer newLecternContainer(int syncId, Inventory inventory, PropertyDelegate delegate, PlayerInventory playerInventory) {
		LecternContainer container = new LecternContainer(syncId, inventory, delegate);
		((LecternContainerAccess)container).setPlayer(((PlayerEntityAccess<CraftPlayer>)playerInventory.player).getBukkit());
		return container;
	}

	public static Slot newArmorSlot(Inventory inventory, int n, EquipmentSlot slot, Identifier textures) {
		return new Slot(inventory, 39 - n, 8, 8 + n * 18) {
			@Override
			public int getMaxStackAmount() {
				return 1;
			}

			@Override
			public boolean canInsert(ItemStack stack) {
				return slot == MobEntity.getPreferredEquipmentSlot(stack);
			}

			@Override
			public boolean canTakeItems(PlayerEntity playerEntity) {
				ItemStack itemStack = this.getStack();
				return (itemStack.isEmpty() || playerEntity.isCreative() || !EnchantmentHelper.hasBindingCurse(itemStack)) && super.canTakeItems(playerEntity);
			}

			@Override
			@Environment (EnvType.CLIENT)
			public Pair<Identifier, Identifier> getBackgroundSprite() {
				return Pair.of(PlayerContainer.BLOCK_ATLAS_TEXTURE, textures);
			}
		};
	}

	public static Slot getOffHandSlot(Inventory inventory) {
		return new Slot(inventory, 40, 77, 62) {
			@Override
			@Environment(EnvType.CLIENT)
			public Pair<Identifier, Identifier> getBackgroundSprite() {
				return Pair.of(PlayerContainer.BLOCK_ATLAS_TEXTURE, PlayerContainer.EMPTY_OFFHAND_ARMOR_SLOT);
			}
		};
	}
}
