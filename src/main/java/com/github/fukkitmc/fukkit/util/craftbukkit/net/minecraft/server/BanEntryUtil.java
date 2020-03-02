package com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.server;

import net.minecraft.server.BanEntry;
import java.util.Date;

public class BanEntryUtil {
	public static boolean isInvalid(BanEntry<?> entry) {
		return entry.getExpiryDate() != null && entry.getExpiryDate().before(new Date());
	}
}
