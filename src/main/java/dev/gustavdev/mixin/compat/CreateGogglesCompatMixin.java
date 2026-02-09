package dev.gustavdev.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Compatibility mixin for Create's GogglesItem to support accessory slots.
 * 
 * IMPORTANT: This mixin affects Create's GogglesItem class specifically.
 * It enables Create's goggles functionality (overlays, block information, etc.)
 * to work when goggles are equipped in accessory slots.
 * 
 * OPTIONAL MIXIN: This mixin is conditionally loaded via GustavdevMixinPlugin.
 * The plugin checks if Create is loaded using FabricLoader.getInstance().isModLoaded("create").
 * If Create is not installed at runtime, this mixin will not be loaded at all.
 * The mod will continue to function normally without this mixin.
 * 
 * How Create goggles work:
 * - Create has a static method to check if a player is wearing goggles
 * - This is typically used for rendering overlays and providing block information
 * - Originally only checks the HEAD equipment slot
 * 
 * This mixin:
 * - Intercepts the goggles equipped check
 * - If HEAD slot doesn't have goggles, checks accessory slots
 * - Returns true if goggles are found in either location
 * 
 * Technical details:
 * - Create uses Architectury/Forge mappings, so remap = false
 * - @Pseudo allows compilation without Create present
 * - Only validated goggles items (via GameplayUtil.isGoggles) are considered
 * - Conditional loading is handled by GustavdevMixinPlugin.shouldApplyMixin()
 */
@Pseudo
@Mixin(targets = "com.simibubi.create.content.equipment.goggles.GogglesItem", remap = false)
public abstract class CreateGogglesCompatMixin {

    /**
     * Modifies the result of isWearingGoggles to include accessory slots.
     * 
     * Create's GogglesItem class has a static method to check if a player
     * is wearing goggles. We intercept this to also check accessory slots.
     * 
     * Original method checks: player.getItemBySlot(EquipmentSlot.HEAD) instanceof GogglesItem
     * 
     * We modify to also check accessory slots if HEAD slot check fails.
     */
    @ModifyReturnValue(
        method = "isWearingGoggles",
        at = @At("RETURN")
    )
    private static boolean checkAccessoryForGoggles(boolean original, Player player) {
        // If already wearing goggles in HEAD slot, return true
        if (original) {
            return true;
        }
        
        // HEAD slot doesn't have goggles, check accessory slots
        ItemStack accessoryGoggles = AccessoryUtil.getAccessoryStack(
            player,
            GameplayUtil::isGoggles
        );
        
        // Return true if goggles found in accessories
        return !accessoryGoggles.isEmpty();
    }
}
