package dev.gustavdev.chunky;

import dev.gustavdev.GustavdevMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.api.ChunkyAPIProvider;

import java.util.concurrent.TimeUnit;

/**
 * Manages automatic world pregeneration using Chunky API.
 * 
 * This manager:
 * - Starts/resumes pregeneration when all players are offline
 * - Pauses pregeneration when a player joins
 * - Implements a startup delay to prevent server lag
 */
public class ChunkyPregenManager {
    private static final long STARTUP_DELAY_SECONDS = 60; // 1 minute delay after server start
    
    private static volatile MinecraftServer server;
    private static ChunkyAPI chunkyAPI;
    private static volatile boolean isInitialized = false;
    private static volatile boolean hasStartupDelayPassed = false;
    
    /**
     * Initialize the Chunky pregeneration manager.
     * Sets up event listeners and startup delay.
     */
    public static void initialize() {
        GustavdevMod.LOGGER.info("Initializing Chunky pregeneration manager");
        
        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(ChunkyPregenManager::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(ChunkyPregenManager::onServerStopping);
        
        // Register player connection events
        ServerPlayConnectionEvents.JOIN.register((handler, sender, joiningServer) -> {
            onPlayerJoin(joiningServer);
        });
        
        ServerPlayConnectionEvents.DISCONNECT.register((handler, disconnectingServer) -> {
            onPlayerDisconnect(disconnectingServer);
        });
        
        isInitialized = true;
        GustavdevMod.LOGGER.info("Chunky pregeneration manager initialized");
    }
    
    /**
     * Called when the server starts.
     * Initializes Chunky API and schedules startup delay.
     */
    private static void onServerStarted(MinecraftServer minecraftServer) {
        server = minecraftServer;
        
        try {
            // Get Chunky API instance
            chunkyAPI = ChunkyAPIProvider.get();
            GustavdevMod.LOGGER.info("Chunky API connected successfully");
            
            // Schedule startup delay check
            scheduleStartupDelay();
            
        } catch (Exception e) {
            GustavdevMod.LOGGER.error("Failed to connect to Chunky API. Make sure Chunky mod is installed.", e);
            chunkyAPI = null;
        }
    }
    
    /**
     * Called when the server is stopping.
     * Pauses any running pregeneration tasks.
     */
    private static void onServerStopping(MinecraftServer minecraftServer) {
        if (chunkyAPI != null) {
            pausePregeneration();
        }
        server = null;
        hasStartupDelayPassed = false;
    }
    
    /**
     * Called when a player joins the server.
     * Pauses pregeneration if it's running.
     */
    private static void onPlayerJoin(MinecraftServer joiningServer) {
        if (!isReady()) {
            return;
        }
        
        GustavdevMod.LOGGER.info("Player joined - pausing pregeneration");
        pausePregeneration();
    }
    
    /**
     * Called when a player disconnects from the server.
     * Starts/resumes pregeneration if no players are online.
     */
    private static void onPlayerDisconnect(MinecraftServer disconnectingServer) {
        if (!isReady()) {
            return;
        }
        
        // Schedule check for next tick to ensure player count is fully updated
        // This prevents race conditions where another player joins immediately
        disconnectingServer.execute(() -> {
            // Use local copy to avoid race condition with onServerStopping
            final MinecraftServer localServer = server;
            // Double-check after player has fully disconnected
            if (localServer != null && localServer.getPlayerCount() == 0) {
                GustavdevMod.LOGGER.info("No players online - resuming pregeneration");
                resumePregeneration();
            }
        });
    }
    
    /**
     * Schedule a task to wait for the startup delay before enabling auto-pregeneration.
     */
    private static void scheduleStartupDelay() {
        GustavdevMod.LOGGER.info("Scheduling startup delay of {} seconds before enabling auto-pregeneration", 
            STARTUP_DELAY_SECONDS);
        
        Thread delayThread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(STARTUP_DELAY_SECONDS);
                
                // Mark startup delay as passed
                hasStartupDelayPassed = true;
                
                // Use local copy to avoid null pointer in case server stops
                final MinecraftServer localServer = server;
                
                // Check if we should start pregeneration (no players online)
                if (localServer != null) {
                    localServer.execute(() -> {
                        // Double-check server is still running and no players are online
                        // Use localServer to avoid race condition with onServerStopping
                        if (localServer.getPlayerCount() == 0) {
                            GustavdevMod.LOGGER.info("Startup delay completed - no players online, starting pregeneration");
                            resumePregeneration();
                        } else {
                            GustavdevMod.LOGGER.info("Startup delay completed - players online, pregeneration will start when they leave");
                        }
                    });
                } else {
                    GustavdevMod.LOGGER.info("Startup delay completed - server already stopped");
                }
                
            } catch (InterruptedException e) {
                GustavdevMod.LOGGER.warn("Startup delay interrupted", e);
                Thread.currentThread().interrupt();
            }
        }, "ChunkyStartupDelay");
        
        delayThread.setDaemon(true);
        delayThread.start();
    }
    
    /**
     * Resume or continue pregeneration tasks.
     */
    private static void resumePregeneration() {
        if (chunkyAPI == null) {
            return;
        }
        
        try {
            // Continue all tasks (this will resume paused tasks or continue running ones)
            chunkyAPI.continueAllTasks();
            GustavdevMod.LOGGER.info("Pregeneration tasks resumed/continued");
        } catch (Exception e) {
            GustavdevMod.LOGGER.error("Failed to resume pregeneration", e);
        }
    }
    
    /**
     * Pause all pregeneration tasks.
     */
    private static void pausePregeneration() {
        if (chunkyAPI == null) {
            return;
        }
        
        try {
            // Pause all running tasks
            chunkyAPI.pauseAllTasks();
            GustavdevMod.LOGGER.info("Pregeneration tasks paused");
        } catch (Exception e) {
            GustavdevMod.LOGGER.error("Failed to pause pregeneration", e);
        }
    }
    
    /**
     * Check if the manager is ready to control pregeneration.
     * @return true if initialized, API is available, and startup delay has passed
     */
    private static boolean isReady() {
        return isInitialized && chunkyAPI != null && hasStartupDelayPassed;
    }
}
