package dev.gustavdev;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Handles integration with the Accessories API.
 * Initializes on mod startup to check if Accessories is available.
 */
public class AccessoriesIntegration implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("gustavdev");
    
    // Cached reflection methods for Accessories API
    private static Method accessoriesCapabilityGetMethod;
    private static Method capabilityGetEquippedMethod;
    private static boolean accessoriesAvailable = false;
    
    @Override
    public void onInitialize() {
        // Check if Accessories mod is available at startup
        accessoriesAvailable = initializeAccessoriesReflection();
        
        if (accessoriesAvailable) {
            LOGGER.info("[GuSMP Resources] Accessories API detected - totem slots will be supported");
        } else {
            LOGGER.info("[GuSMP Resources] Accessories API not found - totems will work in main/off-hand only");
        }
    }
    
    /**
     * Initialize reflection for Accessories API.
     * Called once at mod startup.
     * 
     * @return true if Accessories API is available and reflection setup succeeded, false otherwise
     */
    private static boolean initializeAccessoriesReflection() {
        try {
            // Try to load the AccessoriesCapability class
            Class<?> accessoriesCapabilityClass = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
            
            // Get the static 'get' method: AccessoriesCapability.get(LivingEntity)
            accessoriesCapabilityGetMethod = accessoriesCapabilityClass.getMethod("get", LivingEntity.class);
            
            // Get the 'getEquipped' method from the capability interface
            capabilityGetEquippedMethod = accessoriesCapabilityClass.getMethod("getEquipped", Item.class);
            
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // Accessories mod is not present, which is fine
            return false;
        }
    }
    
    /**
     * Check if Accessories API is available.
     * This is initialized at mod startup.
     * 
     * @return true if Accessories is available, false otherwise
     */
    public static boolean isAccessoriesAvailable() {
        return accessoriesAvailable;
    }
    
    /**
     * Check if item is equipped in an Accessories slot.
     * Uses reflection to avoid compile-time dependency on Accessories API.
     * Only call this if isAccessoriesAvailable() returns true.
     * 
     * @param entity The living entity to check
     * @param item The item to look for
     * @return true if the item is equipped in an accessories slot, false otherwise
     */
    public static boolean isEquippedInAccessoriesSlot(LivingEntity entity, Item item) {
        if (!accessoriesAvailable) {
            return false;
        }
        
        try {
            // Call AccessoriesCapability.get(entity)
            Object capability = accessoriesCapabilityGetMethod.invoke(null, entity);
            
            if (capability == null) {
                return false;
            }
            
            // Call capability.getEquipped(item)
            @SuppressWarnings("unchecked")
            List<?> equippedSlots = (List<?>) capabilityGetEquippedMethod.invoke(capability, item);
            
            // If the list is not empty, the item is equipped
            return equippedSlots != null && !equippedSlots.isEmpty();
            
        } catch (Exception e) {
            // If reflection fails, return false
            return false;
        }
    }
}
