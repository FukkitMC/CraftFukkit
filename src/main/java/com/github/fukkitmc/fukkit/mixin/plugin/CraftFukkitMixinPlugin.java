package com.github.fukkitmc.fukkit.mixin.plugin;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CraftFukkitMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return ImmutableList.of();
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		//Let bukkit access them
		for(FieldNode node:targetClass.fields){
			int access = node.access;
			if((access & Opcodes.ACC_PRIVATE) != 0){
				access = access & (~Opcodes.ACC_PRIVATE);
			}else if((access & Opcodes.ACC_PROTECTED) != 0){
				access = access & (~Opcodes.ACC_PROTECTED);
			}
			node.access = access | Opcodes.ACC_PUBLIC;
		}
		//Handle ChunkTicketTypeMixin
		if(mixinClassName.equals("com.github.fukkitmc.fukkit.mixin.net.minecraft.world.ChunkTicketTypeMixin")){
			targetClass.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC,
					"PLUGIN","Lnet/minecraft/server/world/ChunkTicketType;",null,null).visitEnd();
		}
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
