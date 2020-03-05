package com.github.fukkitmc.fukkit;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ChunkTicketType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// prior to contributing to fukkit
// watch this gif for 1 hr straight
// https://tenor.com/view/weird-face-lick-nose-gif-11424629
public class FukkitInit implements ModInitializer {
    // TODO mixin verification
    // stop being lazy and do all the containers by hand
    // check your TAIL/RETURN, tail is the last return and return is all returns, make sure u did it right
    // remove all traces of arr (bukkit mappings, and preferably any craftbukkit)
    // ensure all my injects aren't accidentally injecting into multiple places
    // make sure I've fully implemented the patch
    // remove overwrites if possible
    // make all captures LocalCapture#FAIL_HARD
    // remove dependency on fabric api (but keep it for testing for conflicts)
    // make sure remPassenger gets an inject
    // replace field locals with thread locals or assert main thread
    // replace thread locals with a FIFO queue or assert main thread
    // comment all :notvanilla: methods with something, and add constraints or use mixin plugin to disable, or just move them to CrapFukkit if they aren't used for anything
    // replace ordinals with slices

    public static boolean useJLine = false;

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "fukkit";
    public static final String MOD_NAME = "fukkit";

    @Override
    public void onInitialize() {
        log(Level.WARN, "Initializing");
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            LOGGER.warn("Warning, clients are not supported, loading multiple worlds at once might cause issues!");
	    initializeMixinAddedField();
    }

	public void initializeMixinAddedField() {
		try {
			ChunkTicketType.class.getField("PLUGIN").set(null, ChunkTicketType.create("plugin",(a, b)-> 0));
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}
