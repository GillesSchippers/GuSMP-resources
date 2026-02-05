package com.gustavserv.gustavdev.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class InventoryManager {
    
    /**
     * Gets the player's inventory. For online players, returns live inventory.
     * For offline players, loads from playerdata.
     */
    public static Inventory getPlayerInventory(MinecraftServer server, UUID playerUuid, String playerName) throws IOException {
        // Try to find online player first
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(playerUuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getInventory();
        }
        
        // Load from playerdata for offline player
        return loadOfflinePlayerInventory(server, playerUuid);
    }
    
    /**
     * Loads an offline player's inventory from their playerdata file
     */
    private static Inventory loadOfflinePlayerInventory(MinecraftServer server, UUID playerUuid) throws IOException {
        File playerDataDir = new File(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR).toFile(), playerUuid.toString() + ".dat");
        
        if (!playerDataDir.exists()) {
            throw new IOException("Player data file not found for UUID: " + playerUuid);
        }
        
        CompoundTag playerData = NbtIo.readCompressed(playerDataDir.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
        
        // Create a temporary inventory and load items
        GameProfile profile = new GameProfile(playerUuid, "");
        Inventory inventory = new Inventory(null);
        
        if (playerData.contains("Inventory", 9)) {
            ListTag inventoryTag = playerData.getList("Inventory", 10);
            inventory.load(inventoryTag);
        }
        
        return inventory;
    }
    
    /**
     * Saves an offline player's inventory back to their playerdata file
     */
    public static void saveOfflinePlayerInventory(MinecraftServer server, UUID playerUuid, Inventory inventory) throws IOException {
        File playerDataFile = new File(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR).toFile(), playerUuid.toString() + ".dat");
        
        if (!playerDataFile.exists()) {
            throw new IOException("Player data file not found for UUID: " + playerUuid);
        }
        
        CompoundTag playerData = NbtIo.readCompressed(playerDataFile.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
        
        // Save inventory to NBT
        ListTag inventoryTag = inventory.save(new ListTag());
        playerData.put("Inventory", inventoryTag);
        
        // Write back to file
        File tempFile = new File(playerDataFile.getParentFile(), playerUuid.toString() + ".dat.tmp");
        NbtIo.writeCompressed(playerData, tempFile.toPath());
        
        // Atomic rename
        File backupFile = new File(playerDataFile.getParentFile(), playerUuid.toString() + ".dat_old");
        if (playerDataFile.exists()) {
            playerDataFile.renameTo(backupFile);
        }
        tempFile.renameTo(playerDataFile);
        if (backupFile.exists()) {
            backupFile.delete();
        }
    }
    
    /**
     * Sets an item in the player's inventory at a specific slot
     */
    public static void setInventorySlot(Inventory inventory, int slot, ItemStack item) {
        if (slot >= 0 && slot < inventory.getContainerSize()) {
            inventory.setItem(slot, item);
        }
    }
    
    /**
     * Gets an item from the player's inventory at a specific slot
     */
    public static ItemStack getInventorySlot(Inventory inventory, int slot) {
        if (slot >= 0 && slot < inventory.getContainerSize()) {
            return inventory.getItem(slot);
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * Clears the player's entire inventory
     */
    public static void clearInventory(Inventory inventory) {
        inventory.clearContent();
    }
}
