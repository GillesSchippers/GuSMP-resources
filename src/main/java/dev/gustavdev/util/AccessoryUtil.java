package dev.gustavdev.util;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Utility class for working with the Accessories API.
 */
public class AccessoryUtil {

    /**
     * Gets an accessory stack from the entity that matches the given predicate.
     * @param entity The entity to check
     * @param predicate The predicate to match accessories against
     * @return The matching ItemStack, or ItemStack.EMPTY if none found
     */
    public static ItemStack getAccessoryStack(LivingEntity entity, Predicate<ItemStack> predicate) {
        var capability = AccessoriesCapability.get(entity);
        if (capability == null) {
            return ItemStack.EMPTY;
        }

        // Get all equipped accessories
        var accessories = capability.getEquipped(predicate);
        
        // Return the first matching accessory, or empty if none found
        if (!accessories.isEmpty()) {
            return accessories.get(0).stack();
        }
        
        return ItemStack.EMPTY;
    }
}
