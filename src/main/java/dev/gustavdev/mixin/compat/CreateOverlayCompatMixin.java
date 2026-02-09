package dev.gustavdev.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Compatibility mixin for Create's goggle overlay rendering to support accessory slots.
 * 
 * IMPORTANT: This mixin affects Create's client-side overlay rendering.
 * It ensures that the goggle overlay appears when goggles are equipped in accessory slots.
 * 
 * OPTIONAL MIXIN: This mixin is conditionally loaded via GustavdevMixinPlugin.
 * The plugin checks if Create is loaded using FabricLoader.getInstance().isModLoaded("create").
 * If Create is not installed at runtime, this mixin will not be loaded at all.
 * 
 * This mixin targets Create's overlay handler classes that render the goggle information.
 * These classes typically check if the player has goggles equipped before rendering overlays.
 * 
 * @Pseudo annotation is used because the target class may not exist (Create is optional)
 * and because different Create versions/forks may have different class structures.
 * 
 * Technical details:
 * - Targets Create's overlay/HUD classes
 * - Intercepts equipment checks for the HEAD slot
 * - Returns goggles from accessories if HEAD slot is empty
 * - Uses @Pseudo to allow compilation without Create present
 */
@Pseudo
@Mixin(targets = {
    "com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer"
}, remap = false)
public abstract class CreateOverlayCompatMixin {

    /**
     * Intercepts getItemBySlot calls in the overlay renderer to include accessory goggles.
     * 
     * Create's overlay renderer checks if the player has goggles via:
     * player.getItemBySlot(EquipmentSlot.HEAD)
     * 
     * We intercept this to also return goggles from accessory slots if HEAD is empty.
     */
    @ModifyExpressionValue(
        method = "*",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
        ),
        require = 0
    )
    private static ItemStack checkAccessoryForGogglesInOverlay(ItemStack original, Player player, EquipmentSlot slot) {
        // Only check for HEAD slot
        if (slot == EquipmentSlot.HEAD) {
            // If HEAD slot already has goggles, use that
            if (!original.isEmpty() && GameplayUtil.isGoggles(original)) {
                return original;
            }
            
            // HEAD slot doesn't have goggles, check accessory slots
            ItemStack accessoryGoggles = AccessoryUtil.getAccessoryStack(
                player,
                GameplayUtil::isGoggles
            );
            
            // If we found goggles in accessories, return them
            if (!accessoryGoggles.isEmpty()) {
                return accessoryGoggles;
            }
        }
        
        // Return the original item stack for all other cases
        return original;
    }
}
