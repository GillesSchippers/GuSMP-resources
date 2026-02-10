package dev.gustavdev.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Compatibility mixin for Create's GogglesItem to support accessory slots.
 * 
 * IMPORTANT: This mixin ONLY affects Create's goggles items. It does not modify
 * vanilla helmet behavior or other mods' head equipment implementations.
 * 
 * OPTIONAL MIXIN: This mixin is conditionally loaded via GustavdevMixinPlugin.
 * The plugin checks if Create is loaded using FabricLoader.getInstance().isModLoaded("create").
 * If Create is not installed at runtime, this mixin will not be loaded at all.
 * The mod will continue to function normally without this mixin.
 * 
 * CREATE-FLY COMPATIBILITY: This mixin targets Create-Fly (com.zurrtum.create), a fork of
 * the original Create mod. Create-Fly uses a different package structure than the original
 * Create mod (com.simibubi.create), so the mixin target has been updated accordingly.
 * 
 * This mixin enables goggles functionality in accessory slots:
 * - HEAD slot: Goggles work (original behavior)
 * - Accessory slot: Goggles work (added by this mixin)
 * 
 * Create's goggles provide overlays showing block information, kinetic stress/speed,
 * and other technical details. This mixin extends that functionality to work when
 * goggles are equipped in an accessory slot instead of the HEAD equipment slot.
 * 
 * Technical details:
 * - Create may use different mappings, Loom handles remapping automatically
 * - Loom automatically remaps @At targets from Mojang to intermediary at compile time
 * - At runtime, both mods use intermediary names, ensuring compatibility
 * - Only validated goggles items (via GameplayUtil.isGoggles) are returned from accessories
 * - Conditional loading is handled by GustavdevMixinPlugin.shouldApplyMixin()
 */
@Mixin(targets = "com.zurrtum.create.content.equipment.goggles.GogglesItem")
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
     * This enables all Create goggles functionality (overlays, block info, etc.)
     * to work when goggles are in an accessory slot.
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
