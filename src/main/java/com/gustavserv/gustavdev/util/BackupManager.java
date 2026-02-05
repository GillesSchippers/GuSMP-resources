package com.gustavserv.gustavdev.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class BackupManager {
    private static final String BACKUP_DIR = "backpacks_backup";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Gets the backup directory for a server
     */
    private static File getBackupDirectory(MinecraftServer server) {
        File worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
        File backupDir = new File(worldDir, BACKUP_DIR);
        
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        return backupDir;
    }
    
    /**
     * Creates a backup of a backpack item
     */
    public static String createBackpackBackup(MinecraftServer server, UUID playerUuid, ItemStack backpack, String backpackName) throws IOException {
        if (backpack.isEmpty()) {
            throw new IllegalArgumentException("Cannot backup empty backpack");
        }
        
        File backupDir = getBackupDirectory(server);
        String timestamp = DATE_FORMAT.format(new Date());
        String fileName = playerUuid.toString() + "_" + backpackName + "_" + timestamp + ".dat";
        File backupFile = new File(backupDir, fileName);
        
        CompoundTag backupData = new CompoundTag();
        backupData.putString("player_uuid", playerUuid.toString());
        backupData.putString("backpack_name", backpackName);
        backupData.putLong("timestamp", System.currentTimeMillis());
        
        CompoundTag backpackTag = new CompoundTag();
        backpack.save(server.registryAccess(), backpackTag);
        backupData.put("backpack", backpackTag);
        
        NbtIo.writeCompressed(backupData, backupFile.toPath());
        
        return fileName;
    }
    
    /**
     * Restores a backpack from a backup file
     */
    public static ItemStack restoreBackpackBackup(MinecraftServer server, String backupFileName) throws IOException {
        File backupDir = getBackupDirectory(server);
        File backupFile = new File(backupDir, backupFileName);
        
        if (!backupFile.exists()) {
            throw new IOException("Backup file not found: " + backupFileName);
        }
        
        CompoundTag backupData = NbtIo.readCompressed(backupFile.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
        
        if (!backupData.contains("backpack")) {
            throw new IOException("Invalid backup file: missing backpack data");
        }
        
        CompoundTag backpackTag = backupData.getCompound("backpack");
        return ItemStack.parseOptional(server.registryAccess(), backpackTag);
    }
    
    /**
     * Lists all backpack backups for a specific player
     */
    public static List<BackupInfo> listPlayerBackups(MinecraftServer server, UUID playerUuid) {
        List<BackupInfo> backups = new ArrayList<>();
        File backupDir = getBackupDirectory(server);
        
        if (!backupDir.exists()) {
            return backups;
        }
        
        File[] files = backupDir.listFiles((dir, name) -> 
            name.startsWith(playerUuid.toString()) && name.endsWith(".dat"));
        
        if (files == null) {
            return backups;
        }
        
        for (File file : files) {
            try {
                CompoundTag backupData = NbtIo.readCompressed(file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                
                BackupInfo info = new BackupInfo();
                info.fileName = file.getName();
                info.playerUuid = UUID.fromString(backupData.getString("player_uuid"));
                info.backpackName = backupData.getString("backpack_name");
                info.timestamp = backupData.getLong("timestamp");
                info.date = new Date(info.timestamp);
                
                backups.add(info);
            } catch (IOException e) {
                // Skip corrupted backup files
            }
        }
        
        // Sort by timestamp, newest first
        backups.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        
        return backups;
    }
    
    /**
     * Lists all backpack backups in the system
     */
    public static List<BackupInfo> listAllBackups(MinecraftServer server) {
        List<BackupInfo> backups = new ArrayList<>();
        File backupDir = getBackupDirectory(server);
        
        if (!backupDir.exists()) {
            return backups;
        }
        
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".dat"));
        
        if (files == null) {
            return backups;
        }
        
        for (File file : files) {
            try {
                CompoundTag backupData = NbtIo.readCompressed(file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                
                BackupInfo info = new BackupInfo();
                info.fileName = file.getName();
                info.playerUuid = UUID.fromString(backupData.getString("player_uuid"));
                info.backpackName = backupData.getString("backpack_name");
                info.timestamp = backupData.getLong("timestamp");
                info.date = new Date(info.timestamp);
                
                backups.add(info);
            } catch (IOException e) {
                // Skip corrupted backup files
            }
        }
        
        // Sort by timestamp, newest first
        backups.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        
        return backups;
    }
    
    /**
     * Deletes a backup file
     */
    public static boolean deleteBackup(MinecraftServer server, String backupFileName) {
        File backupDir = getBackupDirectory(server);
        File backupFile = new File(backupDir, backupFileName);
        
        if (backupFile.exists()) {
            return backupFile.delete();
        }
        
        return false;
    }
    
    /**
     * Information about a backpack backup
     */
    public static class BackupInfo {
        public String fileName;
        public UUID playerUuid;
        public String backpackName;
        public long timestamp;
        public Date date;
        
        @Override
        public String toString() {
            return String.format("%s - %s (Player: %s, Date: %s)", 
                fileName, backpackName, playerUuid, DATE_FORMAT.format(date));
        }
    }
}
