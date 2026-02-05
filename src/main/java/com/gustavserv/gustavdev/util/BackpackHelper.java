package com.gustavserv.gustavdev.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class BackpackHelper {
    
    /**
     * Finds all Traveller's Backpack items in a player's inventory
     */
    public static List<ItemStack> findBackpacksInInventory(Inventory inventory) {
        List<ItemStack> backpacks = new ArrayList<>();
        
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (isBackpack(stack)) {
                backpacks.add(stack);
            }
        }
        
        return backpacks;
    }
    
    /**
     * Finds all Traveller's Backpack items in a player's accessories slots
     */
    public static List<ItemStack> findBackpacksInAccessories(ServerPlayer player) {
        List<ItemStack> backpacks = new ArrayList<>();
        
        var accessories = AccessoriesHelper.getPlayerAccessories(player);
        for (List<ItemStack> items : accessories.values()) {
            for (ItemStack stack : items) {
                if (isBackpack(stack)) {
                    backpacks.add(stack);
                }
            }
        }
        
        return backpacks;
    }
    
    /**
     * Checks if an ItemStack is a Traveller's Backpack
     */
    public static boolean isBackpack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // Check if the item is from travelersbackpack mod
        String itemId = stack.getItem().toString();
        return itemId.contains("travelersbackpack");
    }
    
    /**
     * Gets the inventory contents of a backpack
     */
    public static List<ItemStack> getBackpackContents(ItemStack backpack) {
        List<ItemStack> contents = new ArrayList<>();
        
        if (!isBackpack(backpack)) {
            return contents;
        }
        
        CompoundTag tag = backpack.getTag();
        if (tag == null) {
            return contents;
        }
        
        // Traveller's Backpack stores items in NBT
        if (tag.contains("inventory")) {
            CompoundTag inventoryTag = tag.getCompound("inventory");
            
            // Try to extract items from the inventory compound
            for (String key : inventoryTag.getAllKeys()) {
                if (inventoryTag.get(key) instanceof CompoundTag) {
                    CompoundTag itemTag = inventoryTag.getCompound(key);
                    // This is a simplified version - actual implementation depends on mod internals
                    // ItemStack item = ItemStack.of(itemTag);
                    // if (!item.isEmpty()) {
                    //     contents.add(item);
                    // }
                }
            }
        }
        
        return contents;
    }
    
    /**
     * Sets an item in a backpack at a specific slot
     */
    public static boolean setBackpackSlot(ItemStack backpack, int slot, ItemStack item) {
        if (!isBackpack(backpack)) {
            return false;
        }
        
        CompoundTag tag = backpack.getOrCreateTag();
        
        if (!tag.contains("inventory")) {
            tag.put("inventory", new CompoundTag());
        }
        
        CompoundTag inventoryTag = tag.getCompound("inventory");
        
        // Store item at slot - this is a simplified version
        CompoundTag itemTag = new CompoundTag();
        item.save(backpack.getOrCreateTag().getCompound("RegistryAccess"), itemTag);
        inventoryTag.put("slot_" + slot, itemTag);
        
        return true;
    }
    
    /**
     * Creates a backup NBT of a backpack
     */
    public static CompoundTag createBackpackBackup(ItemStack backpack) {
        CompoundTag backup = new CompoundTag();
        
        if (isBackpack(backpack)) {
            backpack.save(backpack.getOrCreateTag().getCompound("RegistryAccess"), backup);
        }
        
        return backup;
    }
    
    /**
     * Restores a backpack from backup NBT
     */
    public static ItemStack restoreBackpackFromBackup(CompoundTag backup) {
        // This would need the server's registry access to properly restore
        // For now, return empty - implementation depends on context
        return ItemStack.EMPTY;
    }
    
    /**
     * Clears all items from a backpack
     */
    public static void clearBackpack(ItemStack backpack) {
        if (!isBackpack(backpack)) {
            return;
        }
        
        CompoundTag tag = backpack.getTag();
        if (tag != null && tag.contains("inventory")) {
            tag.remove("inventory");
        }
    }
    
    /**
     * Finds a backpack by name or index in player's total backpack list
     */
    public static ItemStack findBackpackByIndex(ServerPlayer player, int index) {
        List<ItemStack> allBackpacks = new ArrayList<>();
        
        // Add backpacks from inventory
        allBackpacks.addAll(findBackpacksInInventory(player.getInventory()));
        
        // Add backpacks from accessories
        allBackpacks.addAll(findBackpacksInAccessories(player));
        
        if (index >= 0 && index < allBackpacks.size()) {
            return allBackpacks.get(index);
        }
        
        return ItemStack.EMPTY;
    }
}
