package com.github.fukkitmc.fukkit.mixin.net.minecraft.block.entity;

import com.github.fukkitmc.fukkit.access.net.minecraft.block.entity.AbstractFurnaceBlockEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.entity.player.PlayerEntityAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.inventory.InventoryAccess;
import com.github.fukkitmc.fukkit.access.net.minecraft.world.WorldAccess;
import com.google.common.collect.Lists;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.InventoryHolder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.github.fukkitmc.fukkit.util.Constants.MAX_STACK;

@Mixin (AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends BlockEntityMixin implements AbstractFurnaceBlockEntityAccess, Inventory {
	private int maxStack = MAX_STACK;
	public List<HumanEntity> transaction = new ArrayList<>();
	@Shadow protected DefaultedList<ItemStack> inventory;

	@Shadow private int burnTime;

	@Shadow
	protected abstract int getFuelTime(ItemStack fuel);

	@Shadow
	protected abstract boolean isBurning();

	@Shadow @Final private Map<Identifier, Integer> recipesUsed;
	private boolean isBurning;

	@Inject (method = "tick", // I'm too lazy to unredirect the method and cancel after
	         at = @At (value = "FIELD", target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;burnTime:I",
	                   opcode = Opcodes.GETFIELD, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_burnEvent(CallbackInfo ci, boolean bl, boolean bl2, ItemStack itemStack, Recipe<?> recipe) {
		CraftItemStack fuel = CraftItemStack.asCraftMirror(itemStack);

		FurnaceBurnEvent furnaceBurnEvent = new FurnaceBurnEvent(CraftBlock
		                                                         .at(this.world, this.pos), fuel,
		                                                         this.getFuelTime(itemStack));
		((WorldAccess) this.world).getBukkitServer().getPluginManager().callEvent(furnaceBurnEvent);

		if (furnaceBurnEvent.isCancelled()) {
			ci.cancel();
			return;
		}

		this.burnTime = furnaceBurnEvent.getBurnTime();
		this.isBurning = furnaceBurnEvent.isBurning();
	}

	@Redirect (method = "tick", at = @At (value = "INVOKE",
	                                      target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;isBurning" +
	                                               "()" +
	                                               "Z", ordinal = 0),
	           slice = @Slice (from = @At (value = "INVOKE",
	                                       target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;" +
	                                                "getFuelTime(Lnet/minecraft/item/ItemStack;)I", ordinal = 0)))
	private boolean fukkit_eventBurning(AbstractFurnaceBlockEntity entity) {
		return this.isBurning() && this.isBurning;
	}

	@Inject (method = "craftRecipe",
	         at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0),
	         locals = LocalCapture.CAPTURE_FAILHARD)
	private void fukkit_overwriteIf(Recipe<?> recipe, CallbackInfo ci, ItemStack stack, ItemStack itemstack1,
	                                ItemStack itemstack2) {
		CraftItemStack source = CraftItemStack.asCraftMirror(stack);
		org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemstack1);

		FurnaceSmeltEvent furnaceSmeltEvent = new FurnaceSmeltEvent(((WorldAccess)this.world).getBukkit()
		                                                                      .getBlockAt(this.pos.getX(), this.pos
		                                                                                                   .getY(),
		                                                                                  this.pos
		                                                                                                            .getZ()), source, result);
		((WorldAccess)this.world).getBukkitServer().getPluginManager().callEvent(furnaceSmeltEvent);

		if (furnaceSmeltEvent.isCancelled()) {
			ci.cancel();
			return;
		}

		result = furnaceSmeltEvent.getResult();
		itemstack1 = CraftItemStack.asNMSCopy(result);

		if (!itemstack1.isEmpty()) {
			if (itemstack2.isEmpty()) {
				this.inventory.set(2, itemstack1.copy());
			} else if (CraftItemStack.asCraftMirror(itemstack2).isSimilar(result)) {
				itemstack2.increment(itemstack1.getCount());
			} else {
				ci.cancel();
			}
		}
	}

	@Redirect (method = "craftRecipe",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0))
	private boolean fukkit_overwriteIf(ItemStack stack) {
		return false;
	}

	@Redirect (method = "craftRecipe",
	           at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;increment(I)V"))
	private void fukkit_overwriteIf(ItemStack stack, int amount) {}

	// NMS compat dropExperience
	public void d(PlayerEntity player, ItemStack itemstack, int amount) {
		this.dropExperience(player, itemstack, amount);
	}

	// NMS compat dropExperience
	private void a(PlayerEntity player, int i, float f, ItemStack itemstack, int amount) {
		this.dropExperience(player, i, f, itemstack, amount);
	}

	@Override
	public void dropExperience(PlayerEntity player, ItemStack stack, int amount) { // CraftBukkit
		List<Recipe<?>> list = Lists.newArrayList();
		Iterator iterator = this.recipesUsed.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<Identifier, Integer> entry = (Map.Entry) iterator.next();

			player.world.getRecipeManager().get(entry.getKey()).ifPresent((irecipe) -> {
				list.add(irecipe);
				this.dropExperience(player, entry.getValue(), ((AbstractCookingRecipe) irecipe).getCookTime(), stack, amount); // CraftBukkit
			});
		}

		player.unlockRecipes(list);
		this.recipesUsed.clear();
	}

	private void dropExperience(PlayerEntity player, int i, float f, ItemStack itemstack, int amount) { // CraftBukkit
		int j;

		if (f == 0.0F) {
			i = 0;
		} else if (f < 1.0F) {
			j = MathHelper.floor((float) i * f);
			if (j < MathHelper.ceil((float) i * f) && Math.random() < (double) ((float) i * f - (float) j)) {
				++j;
			}

			i = j;
		}

		// CraftBukkit start - fire FurnaceExtractEvent
		if (amount != 0) {
			FurnaceExtractEvent event = new FurnaceExtractEvent(((PlayerEntityAccess<CraftPlayer>)player).getBukkit(), CraftBlock.at(this.world, this.pos), org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(itemstack.getItem()), amount, i);
			((WorldAccess)this.world).getBukkitServer().getPluginManager().callEvent(event);
			i = event.getExpToDrop();
		}
		// CraftBukkit end

		while (i > 0) {
			j = ExperienceOrbEntity.roundToOrbSize(i);
			i -= j;
			player.world.spawnEntity(new ExperienceOrbEntity(player.world, player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, j));
		}

	}


	@Override
	public List<ItemStack> getContents() {
		return this.inventory;
	}

	@Override
	public void onOpen(CraftHumanEntity who) {
		this.transaction.add(who);
	}

	@Override
	public void onClose(CraftHumanEntity who) {
		this.transaction.remove(who);
	}

	@Override
	public List<HumanEntity> getViewers() {
		return this.transaction;
	}

	@Override
	public InventoryHolder getOwner() {
		return super.getOwner();
	}

	@Override
	public void setMaxStackSize(int size) {
		this.maxStack = size;
	}

	@Override
	public int getInvMaxStackAmount() {
		return this.maxStack;
	}

}
