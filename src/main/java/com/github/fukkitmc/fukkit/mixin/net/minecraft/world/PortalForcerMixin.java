package com.github.fukkitmc.fukkit.mixin.net.minecraft.world;

import com.github.fukkitmc.fukkit.access.net.minecraft.entity.EntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.PortalForcerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Implements (@Interface (iface = PortalForcerAccess.class, prefix = "fukkit$"))
@Mixin (PortalForcer.class)
public class PortalForcerMixin {
	@Shadow
	@Final
	private ServerWorld world;

	@Shadow @Final private Random random;

	public BlockPattern.TeleportTarget fukkit$findAndTeleport(Entity entity, BlockPos pos, float yawAccess, int searchRadius, boolean searchOnly) {
		Vec3d vec3d = entity.getLastNetherPortalDirectionVector();
		Direction direction = entity.getLastNetherPortalDirection();
		BlockPattern.TeleportTarget teleportTarget = this.fukkit$findPortal(pos, entity.getVelocity(), direction, vec3d.x, vec3d.y, entity instanceof PlayerEntity, searchRadius); // CraftBukkit - add location and searchRadius
		if (searchOnly) return teleportTarget; // CraftBukkit - optional teleporting

		if (teleportTarget == null) {
			return null;
		} else {
			Vec3d position = teleportTarget.pos;
			Vec3d velocity = teleportTarget.velocity;

			entity.setVelocity(velocity);
			entity.yaw = yawAccess + (float) teleportTarget.yaw;
			entity.positAfterTeleport(position.x, position.y, position.z);
			return teleportTarget;
		}
	}

	public BlockPattern.TeleportTarget fukkit$findPortal(BlockPos searchStart, Vec3d vec3d, Direction enumdirection, double d0, double d1, boolean flag, int searchRadius) {
		PointOfInterestStorage poiStorage = this.world.getPointOfInterestStorage();
		poiStorage.method_22439(this.world, searchStart, 128);
		List<PointOfInterest> list = poiStorage.method_22383((villageplacetype) -> villageplacetype == PointOfInterestType.NETHER_PORTAL, searchStart, searchRadius, PointOfInterestStorage.OccupationStatus.ANY).collect(Collectors.toList()); // CraftBukkit - searchRadius
		Optional<PointOfInterest> optional = list.stream().min(Comparator.<PointOfInterest>comparingDouble((record) -> record.getPos().getSquaredDistance(searchStart)).thenComparingInt((villageplacerecord) -> villageplacerecord.getPos().getY()));
		return optional.map((poi) -> {
			BlockPos pos = poi.getPos();

			this.world.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(pos), 3, pos);
			BlockPattern.Result result = NetherPortalBlock.findPortal(this.world, pos);

			return result.getTeleportTarget(enumdirection, pos, d1, vec3d, d0);
		}).orElse(null); // CraftBukkit - decompile error
	}

	// md5pls
	public boolean fukkit$createPortal(Entity entity, BlockPos createPosition, int createRadius) {
		// CraftBukkit end
		double d0 = -1.0D;
		// CraftBukkit start - providable position
		int i = createPosition.getX();
		int j = createPosition.getY();
		int k = createPosition.getZ();
		// CraftBukkit end
		int l = i;
		int i1 = j;
		int j1 = k;
		int k1 = 0;
		int l1 = this.random.nextInt(4);
		BlockPos.Mutable pos = new BlockPos.Mutable();

		double d1;
		int i2;
		double d2;
		int j2;
		int k2;
		int l2;
		int i3;
		int j3;
		int k3;
		int l3;
		int i4;
		int j4;
		int k4;
		double d3;
		double d4;

		for (i2 = i - createRadius; i2 <= i + createRadius; ++i2) { // CraftBukkit - createRadius
			d1 = (double) i2 + 0.5D - createPosition.getX(); // CraftBukkit - providable position

			for (j2 = k - createRadius; j2 <= k + createRadius; ++j2) { // CraftBukkit - createRadius
				d2 = (double) j2 + 0.5D - createPosition.getZ(); // CraftBukkit - providable position

				label257:
				for (k2 = this.world.getEffectiveHeight() - 1; k2 >= 0; --k2) {
					if (this.world.isAir(pos.set(i2, k2, j2))) {
						while (k2 > 0 && this.world.isAir(pos.set(i2, k2 - 1, j2))) {
							--k2;
						}

						for (i3 = l1; i3 < l1 + 4; ++i3) {
							l2 = i3 % 2;
							j3 = 1 - l2;
							if (i3 % 4 >= 2) {
								l2 = -l2;
								j3 = -j3;
							}

							for (l3 = 0; l3 < 3; ++l3) {
								for (i4 = 0; i4 < 4; ++i4) {
									for (k4 = -1; k4 < 4; ++k4) {
										k3 = i2 + (i4 - 1) * l2 + l3 * j3;
										j4 = k2 + k4;
										int l4 = j2 + (i4 - 1) * j3 - l3 * l2;

										pos.set(k3, j4, l4);
										if (k4 < 0 && !this.world.getBlockState(pos).getMaterial().isSolid() || k4 >= 0 && !this.world.isAir(pos)) {
											continue label257;
										}
									}
								}
							}

							d3 = (double) k2 + 0.5D - entity.getY();
							d4 = d1 * d1 + d3 * d3 + d2 * d2;
							if (d0 < 0.0D || d4 < d0) {
								d0 = d4;
								l = i2;
								i1 = k2;
								j1 = j2;
								k1 = i3 % 4;
							}
						}
					}
				}
			}
		}

		if (d0 < 0.0D) {
			for (i2 = i - createRadius; i2 <= i + createRadius; ++i2) { // CraftBukkit - createRadius
				d1 = (double) i2 + 0.5D - createPosition.getX(); // CraftBukkit - providable position

				for (j2 = k - createRadius; j2 <= k + createRadius; ++j2) { // CraftBukkit - createRadius
					d2 = (double) j2 + 0.5D - createPosition.getZ(); // CraftBukkit - providable position

					label205:
					for (k2 = this.world.getEffectiveHeight() - 1; k2 >= 0; --k2) {
						if (this.world.isAir(pos.set(i2, k2, j2))) {
							while (k2 > 0 && this.world.isAir(pos.set(i2, k2 - 1, j2))) {
								--k2;
							}

							for (i3 = l1; i3 < l1 + 2; ++i3) {
								l2 = i3 % 2;
								j3 = 1 - l2;

								for (l3 = 0; l3 < 4; ++l3) {
									for (i4 = -1; i4 < 4; ++i4) {
										k4 = i2 + (l3 - 1) * l2;
										k3 = k2 + i4;
										j4 = j2 + (l3 - 1) * j3;
										pos.set(k4, k3, j4);
										if (i4 < 0 && !this.world.getBlockState(pos).getMaterial().isSolid() || i4 >= 0 && !this.world.isAir(pos)) {
											continue label205;
										}
									}
								}

								d3 = (double) k2 + 0.5D - entity.getY();
								d4 = d1 * d1 + d3 * d3 + d2 * d2;
								if (d0 < 0.0D || d4 < d0) {
									d0 = d4;
									l = i2;
									i1 = k2;
									j1 = j2;
									k1 = i3 % 2;
								}
							}
						}
					}
				}
			}
		}

		int i5 = l;
		int j5 = i1;

		j2 = j1;
		int k5 = k1 % 2;
		int l5 = 1 - k5;

		if (k1 % 4 >= 2) {
			k5 = -k5;
			l5 = -l5;
		}

		org.bukkit.craftbukkit.util.BlockStateListPopulator blockList = new org.bukkit.craftbukkit.util.BlockStateListPopulator(this.world); // CraftBukkit - Use BlockStateListPopulator
		if (d0 < 0.0D) {
			i1 = MathHelper.clamp(i1, 70, this.world.getEffectiveHeight() - 10);
			j5 = i1;

			for (k2 = -1; k2 <= 1; ++k2) {
				for (i3 = 1; i3 < 3; ++i3) {
					for (l2 = -1; l2 < 3; ++l2) {
						j3 = i5 + (i3 - 1) * k5 + k2 * l5;
						l3 = j5 + l2;
						i4 = j2 + (i3 - 1) * l5 - k2 * k5;
						boolean flag1 = l2 < 0;

						pos.set(j3, l3, i4);
						blockList.setBlockState(pos, flag1 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState(), 3); // CraftBukkit
					}
				}
			}
		}

		for (k2 = -1; k2 < 3; ++k2) {
			for (i3 = -1; i3 < 4; ++i3) {
				if (k2 == -1 || k2 == 2 || i3 == -1 || i3 == 3) {
					pos.set(i5 + k2 * k5, j5 + i3, j2 + k2 * l5);
					blockList.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 3); // CraftBukkit
				}
			}
		}

		BlockState iblockdata = Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, k5 == 0 ? Direction.Axis.Z : Direction.Axis.X);

		for (i3 = 0; i3 < 2; ++i3) {
			for (l2 = 0; l2 < 3; ++l2) {
				pos.set(i5 + i3 * k5, j5 + l2, j2 + i3 * l5);
				blockList.setBlockState(pos, iblockdata, 18); // CraftBukkit
			}
		}

		// CraftBukkit start
		org.bukkit.World bworld = ((WorldAccess)this.world).getBukkit();
		org.bukkit.event.world.PortalCreateEvent event = new org.bukkit.event.world.PortalCreateEvent((java.util.List<org.bukkit.block.BlockState>) (java.util.List) blockList.getList(), bworld, ((EntityAccess<?>)entity).getBukkit(), org.bukkit.event.world.PortalCreateEvent.CreateReason.NETHER_PAIR);

		((WorldAccess)this.world).getBukkitServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			blockList.updateList();
		}
		// CraftBukkit end
		return true;
	}

}
