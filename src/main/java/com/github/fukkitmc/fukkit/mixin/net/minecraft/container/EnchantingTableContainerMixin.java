package com.github.fukkitmc.fukkit.mixin.net.minecraft.container;

import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.container.BlockContextContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.container.ContainerAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Container;
import net.minecraft.container.EnchantingTableContainer;
import net.minecraft.enchantment.InfoEnchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryEnchanting;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.List;
import java.util.Map;

@Mixin (EnchantingTableContainer.class)
public abstract class EnchantingTableContainerMixin extends ContainerMixin implements ContainerAccess,
                                                                                      BlockContextContainerAccess {
	@Shadow @Final private BlockContext context;

	@Shadow @Final public int[] enchantmentId;

	@Shadow @Final public int[] enchantmentLevel;

	@Shadow @Final public int[] enchantmentPower;

	@Redirect (method = "onContentChanged",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEnchantable()Z"))
	private boolean fukkit_relax(ItemStack stack) {
		return true;
	}

	private CraftInventoryView view;
	public PlayerInventory inventory;

	@Inject (
	method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/container/BlockContext;)V",
	at = @At ("TAIL"))
	private void fukkit_initPlayer(int syncId, PlayerInventory playerInventory, BlockContext blockContext,
	                               CallbackInfo ci) {
		this.inventory = playerInventory;
	}

	private int bookshelves;

	@Inject (
	method = "method_17411(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;" +
	         "Lnet/minecraft/util/math/BlockPos;" +
	         ")V",
	at = @At (value = "INVOKE", target = "Ljava/util/Random;setSeed(J)V"),
	locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void fukkit_shelves(ItemStack stack, World world, BlockPos pos, CallbackInfo info, int shelves) {
		this.bookshelves = shelves;
	}

	@Inject (
	method = "method_17411(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;" +
	         "Lnet/minecraft/util/math/BlockPos;" +
	         ")V",
	at = @At (value = "INVOKE", target = "Lnet/minecraft/container/EnchantingTableContainer;sendContentUpdates()V"),
	cancellable = true)
	private void fukkit_prepareEvent(ItemStack stack, World world, BlockPos pos, CallbackInfo info) {
		CraftItemStack item = CraftItemStack.asCraftMirror(stack);
		EnchantmentOffer[] offers = new EnchantmentOffer[3];

		for (int j = 0; j < 3; ++j) {
			org.bukkit.enchantments.Enchantment enchantment =
			(this.enchantmentId[j] >= 0) ? org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey
			                                                                            .fromMinecraft(Registry.ENCHANTMENT
			                                                                                           .getId(Registry.ENCHANTMENT
			                                                                                                  .get(this.enchantmentId[j])))) :
			null;
			offers[j] = (enchantment != null) ?
			            new EnchantmentOffer(enchantment, this.enchantmentLevel[j], this.enchantmentPower[j]) : null;
		}

		PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(((PlayerEntityAccess<CraftPlayer>)this.inventory.player).getBukkit(), this
		                                                                              .getBukkitView(),
		                                                            ((BlockContextAccess) this.context)
		                                                            .getLocation()
		                                                            .getBlock(),
		                                                            item, offers, this.bookshelves);
		event.setCancelled(!stack.isEnchantable());
		((WorldAccess) world).getBukkitServer().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			for (int j = 0; j < 3; ++j) {
				this.enchantmentPower[j] = 0;
				this.enchantmentId[j] = -1;
				this.enchantmentLevel[j] = -1;
			}
			return;
		}

		for (int j = 0; j < 3; j++) {
			EnchantmentOffer offer = event.getOffers()[j];
			if (offer != null) {
				this.enchantmentPower[j] = offer.getCost();
				this.enchantmentId[j] = Registry.ENCHANTMENT.getRawId(Registry.ENCHANTMENT.get(CraftNamespacedKey
				                                                                               .toMinecraft(offer
				                                                                                            .getEnchantment()
				                                                                                            .getKey())));
				this.enchantmentLevel[j] = offer.getEnchantmentLevel();
			} else {
				this.enchantmentPower[j] = 0;
				this.enchantmentId[j] = -1;
				this.enchantmentLevel[j] = -1;
			}
		}
	}

	@Redirect (method = "method_17410", at = @At (value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
	private boolean fukkit_relax(List list) {
		return false;
	}

	@Redirect (method = "method_17410", at = @At (value = "INVOKE",
	                                               target = "Lnet/minecraft/entity/player/PlayerEntity;" +
	                                                        "applyEnchantmentCosts(Lnet/minecraft/item/ItemStack;I)V"))
	private void fukkit_movedDown(PlayerEntity entity, ItemStack enchantedItem, int experienceLevels) {}

	private EnchantItemEvent event;
	private CraftItemStack stack;

	@Inject (method = "method_17410",
	         at = @At (value = "FIELD", target = "Lnet/minecraft/item/Items;BOOK:Lnet/minecraft/item/Item;",
	                   shift = At.Shift.AFTER),
	         locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void fukkit_enchants(ItemStack itemStack, int i, PlayerEntity playerEntity, int j, ItemStack arg4,
	                             World arg5, BlockPos arg6, CallbackInfo ci, ItemStack itemStack3, List list) {
		Map<Enchantment, Integer> enchants =
		new java.util.HashMap<>();
		for (Object obj : list) {
			InfoEnchantment instance = (InfoEnchantment) obj;
			enchants.put(org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey
			                                                          .fromMinecraft(Registry.ENCHANTMENT
			                                                                         .getId(instance.enchantment))),
			             instance.level);
		}
		this.stack = CraftItemStack.asCraftMirror(itemStack3);

		EnchantItemEvent event = new EnchantItemEvent(((PlayerEntityAccess<CraftPlayer>) playerEntity).getBukkit(),
		                                              this
		                                              .getBukkitView(), ((BlockContextAccess) this.context)
		                                                                .getLocation()
		                                                                .getBlock(), this.stack, this.enchantmentPower[i],
		                                              enchants, i);
		((WorldAccess) arg5).getBukkitServer().getPluginManager().callEvent(event);
		this.event = event;

		int level = event.getExpLevelCost();
		if (event
		    .isCancelled() || (level > playerEntity.experienceLevel && !playerEntity.abilities.creativeMode) || event
		                                                                                                        .getEnchantsToAdd()
		                                                                                                        .isEmpty()) {
			ci.cancel();
		}
	}



	@Inject (method = "method_17410", at = @At (value = "INVOKE", target = "Ljava/util/List;size()I"),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_overwriteLoop(ItemStack itemStack, int i, PlayerEntity playerEntity, int j, ItemStack arg4,
	                                  World arg5, BlockPos arg6, CallbackInfo ci, ItemStack itemStack3, List<?> list,
	                                  boolean bl, int k) {
		for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : this.event.getEnchantsToAdd().entrySet()) {
			try {
				if (bl) {
					NamespacedKey enchantId = entry.getKey().getKey();
					net.minecraft.enchantment.Enchantment nms = Registry.ENCHANTMENT.get(CraftNamespacedKey.toMinecraft(enchantId));
					if (nms == null) {
						continue;
					}

					InfoEnchantment enchantment = new InfoEnchantment(nms, entry.getValue());
					EnchantedBookItem.addEnchantment(itemStack3, enchantment);
				} else {
					this.stack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
				}
			} catch (IllegalArgumentException e) {
				/* Just swallow invalid enchantments */
			}
		}
	}
	@Redirect (method = "method_17410", at = @At (value = "INVOKE", target = "Ljava/util/List;size()I"))
	private int fukkit_overwriteLoop(List yes) {
		return 0;
	}

	@Inject(method = "method_17410", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;creativeMode:Z"))
	private void fukkit_applyEnchantCosts(ItemStack itemStack, int i, PlayerEntity playerEntity, int j, ItemStack arg4,
	                                      World arg5, BlockPos arg6, CallbackInfo ci) {
		playerEntity.applyEnchantmentCosts(itemStack, j);
	}

	@Override
	public InventoryView getBukkitView() {
		if (this.view != null) { return this.view; }
		this.view = new CraftInventoryView(((PlayerEntityAccess<?>) this.inventory.player).getBukkit(), new CraftInventoryEnchanting(this.inventory),
		                                   (Container) (Object) this);
		return this.view;
	}

	@Inject (method = "canUse", at = @At ("HEAD"))
	private void fukkit_canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (!this.checkReachable) { cir.setReturnValue(true); }
	}

	@Override
	public BlockContext getContext() {
		return this.context;
	}
}
