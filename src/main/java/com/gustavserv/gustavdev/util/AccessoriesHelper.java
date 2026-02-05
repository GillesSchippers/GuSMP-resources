package com.gustavserv.gustavdev.util;

import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AccessoriesHelper {
    
    /**
     * Gets all accessories slots for a player
     */
    public static Map<String, List<ItemStack>> getPlayerAccessories(ServerPlayer player) {
        Map<String, List<ItemStack>> accessories = new HashMap<>();
        
        Optional<AccessoriesCapability> capabilityOptional = AccessoriesCapability.get(player);
        if (capabilityOptional.isEmpty()) {
            return accessories;
        }
        
        AccessoriesCapability capability = capabilityOptional.get();
        Map<String, io.wispforest.accessories.api.AccessoriesContainer> containers = capability.getContainers();
        
        for (Map.Entry<String, io.wispforest.accessories.api.AccessoriesContainer> entry : containers.entrySet()) {
            String slotType = entry.getKey();
            io.wispforest.accessories.api.AccessoriesContainer container = entry.getValue();
            List<ItemStack> items = new ArrayList<>();
            
            for (int i = 0; i < container.getSize(); i++) {
                ItemStack stack = container.getAccessories().getItem(i);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
            
            if (!items.isEmpty()) {
                accessories.put(slotType, items);
            }
        }
        
        return accessories;
    }
    
    /**
     * Sets an item in a specific accessories slot
     */
    public static boolean setAccessorySlot(ServerPlayer player, String slotType, int index, ItemStack item) {
        Optional<AccessoriesCapability> capabilityOptional = AccessoriesCapability.get(player);
        if (capabilityOptional.isEmpty()) {
            return false;
        }
        
        AccessoriesCapability capability = capabilityOptional.get();
        var container = capability.getContainers().get(slotType);
        
        if (container == null) {
            return false;
        }
        
        if (index >= 0 && index < container.getSize()) {
            container.getAccessories().setItem(index, item);
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets an item from a specific accessories slot
     */
    public static ItemStack getAccessorySlot(ServerPlayer player, String slotType, int index) {
        Optional<AccessoriesCapability> capabilityOptional = AccessoriesCapability.get(player);
        if (capabilityOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        AccessoriesCapability capability = capabilityOptional.get();
        var container = capability.getContainers().get(slotType);
        
        if (container == null) {
            return ItemStack.EMPTY;
        }
        
        if (index >= 0 && index < container.getSize()) {
            return container.getAccessories().getItem(index);
        }
        
        return ItemStack.EMPTY;
    }
    
    /**
     * Clears all accessories from a player
     */
    public static void clearAllAccessories(ServerPlayer player) {
        Optional<AccessoriesCapability> capabilityOptional = AccessoriesCapability.get(player);
        if (capabilityOptional.isEmpty()) {
            return;
        }
        
        AccessoriesCapability capability = capabilityOptional.get();
        Map<String, io.wispforest.accessories.api.AccessoriesContainer> containers = capability.getContainers();
        
        for (io.wispforest.accessories.api.AccessoriesContainer container : containers.values()) {
            container.getAccessories().clearContent();
        }
    }
    
    /**
     * Lists all available accessory slot types for a player
     */
    public static List<String> getAvailableSlotTypes(ServerPlayer player) {
        Optional<AccessoriesCapability> capabilityOptional = AccessoriesCapability.get(player);
        if (capabilityOptional.isEmpty()) {
            return Collections.emptyList();
        }
        
        AccessoriesCapability capability = capabilityOptional.get();
        return new ArrayList<>(capability.getContainers().keySet());
    }
    
    /**
     * Loads accessories data from offline player NBT
     */
    public static Map<String, List<ItemStack>> loadOfflineAccessories(MinecraftServer server, UUID playerUuid) throws IOException {
        File playerDataFile = new File(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR).toFile(), playerUuid.toString() + ".dat");
        
        if (!playerDataFile.exists()) {
            throw new IOException("Player data file not found for UUID: " + playerUuid);
        }
        
        CompoundTag playerData = NbtIo.readCompressed(playerDataFile.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
        Map<String, List<ItemStack>> accessories = new HashMap<>();
        
        // Try to load accessories data from NBT
        if (playerData.contains("accessories", 10)) {
            CompoundTag accessoriesTag = playerData.getCompound("accessories");
            
            for (String key : accessoriesTag.getAllKeys()) {
                ListTag slotList = accessoriesTag.getList(key, 10);
                List<ItemStack> items = new ArrayList<>();
                
                for (int i = 0; i < slotList.size(); i++) {
                    CompoundTag itemTag = slotList.getCompound(i);
                    ItemStack stack = ItemStack.parseOptional(server.registryAccess(), itemTag);
                    if (!stack.isEmpty()) {
                        items.add(stack);
                    }
                }
                
                if (!items.isEmpty()) {
                    accessories.put(key, items);
                }
            }
        }
        
        return accessories;
    }
}
