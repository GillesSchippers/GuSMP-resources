package dev.gustavdev.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class for gameplay-related item checks.
 * Based on Accessorify mod by pajicadvance.
 */
public class GameplayUtil {

    private static final TagKey<Item> TOTEM_TAG = TagKey.create(
        BuiltInRegistries.ITEM.key(),
        Identifier.fromNamespaceAndPath("accessories", "totem")
    );

    /**
     * Checks if the given ItemStack is a totem.
     * 
     * Matches Accessorify's implementation for 1.21.10+:
     * - Checks the accessories:totem tag (for configured totems)
     * - Also checks for DEATH_PROTECTION component (for any item with totem functionality)
     * 
     * This ensures compatibility with:
     * - Vanilla totem of undying
     * - Custom totems from Friends and Foes
     * - Any modded item with death protection component
     * 
     * @param stack The ItemStack to check
     * @return true if the stack is a totem, false otherwise
     */
    public static boolean isTotem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // Check both the tag and the DEATH_PROTECTION component
        // This matches Accessorify 1.21.10+ behavior
        return stack.is(TOTEM_TAG) || stack.has(DataComponents.DEATH_PROTECTION);
    }
}
