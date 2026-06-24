package xyz.inf32768.ultimatescaler;

import net.fabricmc.api.ModInitializer;

public class UltimateScaler implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModMetadata.LOGGER.info("Hello Fabric world!");
    }
}
