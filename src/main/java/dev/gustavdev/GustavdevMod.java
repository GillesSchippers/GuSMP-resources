package dev.gustavdev;

import dev.gustavdev.chunky.ChunkyPregenManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the GuSMP resources mod.
 */
public class GustavdevMod implements ModInitializer {
    public static final String MOD_ID = "gustavdev";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing GuSMP resources mod");
        
        // Initialize Chunky pregeneration manager
        ChunkyPregenManager.initialize();
        
        LOGGER.info("GuSMP resources mod initialized successfully");
    }
}
