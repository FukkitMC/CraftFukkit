package com.github.fukkitmc.fukkit.injectors;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

@InjectionPoint.AtCode ("OPCODE")
public class GenericOpcode extends InjectionPoint {
	private static final String[] BLACKLIST = {"opcode", "target", "ordinal"};
	private static final Logger LOGGER = Logger.getLogger(GenericOpcode.class.getSimpleName());
	private static final Field INJECTION_ARGS;

	static {
		Arrays.sort(BLACKLIST);
		try {
			Field field = InjectionPointData.class.getDeclaredField("args");
			field.setAccessible(true);
			INJECTION_ARGS = field;
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private int opcode;
	private Map<String, String> args;

	public GenericOpcode(InjectionPointData data) throws IllegalAccessException {
		super(data);
		this.opcode = data.getOpcode();
		this.args = (Map<String, String>) INJECTION_ARGS.get(data);
	}

	@Override
	public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
		boolean found = false;
		label:
		for (AbstractInsnNode insn : insns) {
			if (insn.getOpcode() == this.opcode) {
				Class<?> type = insn.getClass();
				for (Map.Entry<String, String> entry : this.args.entrySet()) {
					String arg = entry.getKey();
					String val = entry.getValue();
					if (Arrays.binarySearch(BLACKLIST, arg) == -1) {
						try {
							Field field = type.getDeclaredField(arg);
							field.setAccessible(true);
							String name = field.get(insn).toString();
							if (!val.equals(name)) {
								continue label; // does not match field
							}
						} catch (NoSuchFieldException | IllegalAccessException e) {
							LOGGER.warning("no field in " + type + " found for name " + arg);
							throw new RuntimeException(e);
						}
					}
				}
				nodes.add(insn);
				found = true;
			}
		}
		return found;
	}
}