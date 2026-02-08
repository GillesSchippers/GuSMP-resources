package dev.gustavdev.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class for gameplay-related item checks.
 */
public class GameplayUtil {

    private static final TagKey<Item> TOTEM_TAG = TagKey.create(
        BuiltInRegistries.ITEM.key(),
        new ResourceLocation("accessories", "totem")
    );

    /**
     * Checks if the given ItemStack is a totem.
     * @param stack The ItemStack to check
     * @return true if the stack is a totem, false otherwise
     */
    public static boolean isTotem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(TOTEM_TAG);
    }
}
