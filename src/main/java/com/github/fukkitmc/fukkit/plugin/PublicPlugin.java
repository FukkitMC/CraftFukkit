package com.github.fukkitmc.fukkit.plugin;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Set;

public class PublicPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {}
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override
	public List<String> getMixins() {
		return null;
	}
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		targetClass.access = widen(targetClass.access);
		for (FieldNode field : targetClass.fields) {
			field.access = widen(field.access);
		}
		for (MethodNode method : targetClass.methods) {
			method.access = widen(method.access);
		}
	}

	private static int widen(int mod) {
		return (mod & ~0b111) | 0b1;
	}
}
