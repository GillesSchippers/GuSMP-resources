package com.gustavserv.gustavdev;

import com.gustavserv.gustavdev.commands.EditInventoryCommand;
import com.gustavserv.gustavdev.commands.BackpackBackupCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GustavDevMod implements ModInitializer {
    public static final String MOD_ID = "gustavdev";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing GuSMP Resources Mod");
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EditInventoryCommand.register(dispatcher);
            BackpackBackupCommand.register(dispatcher);
        });
        
        LOGGER.info("GuSMP Resources Mod initialized successfully!");
    }
}
