package dev.gustavdev.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Compatibility mixin for Create's goggle overlay rendering to support accessory slots.
 * 
 * CLIENT-SIDE ONLY: This mixin targets Create's client-side overlay renderer.
 * It is registered in the "client" array of gustavdev.mixins.json and will only
 * load on the client, preventing ClassNotFoundException on dedicated servers.
 * 
 * IMPORTANT: This mixin ONLY affects Create's client-side overlay rendering.
 * It does not modify vanilla rendering or other mods' overlay implementations.
 * 
 * OPTIONAL MIXIN: This mixin is conditionally loaded via GustavdevMixinPlugin.
 * The plugin checks if Create is loaded using FabricLoader.getInstance().isModLoaded("create").
 * If Create is not installed at runtime, this mixin will not be loaded at all.
 * The mod will continue to function normally without this mixin.
 * 
 * This mixin enables Create's goggle overlay to render when goggles are in accessory slots:
 * - HEAD slot: Overlay renders (original behavior)
 * - Accessory slot: Overlay renders (added by this mixin)
 * 
 * Create's overlay renderer checks if the player has goggles equipped before rendering
 * block information, stress/speed data, and other technical details. This mixin extends
 * that check to include accessory slots.
 * 
 * Technical details:
 * - Create may use different mappings, Loom handles remapping automatically
 * - Loom automatically remaps @At targets from Mojang to intermediary at compile time
 * - At runtime, both mods use intermediary names, ensuring compatibility
 * - Only validated goggles items (via GameplayUtil.isGoggles) are returned from accessories
 * - Conditional loading is handled by GustavdevMixinPlugin.shouldApplyMixin()
 */
@Mixin(targets = "com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer")
public abstract class CreateOverlayCompatMixin {

    /**
     * Intercepts getItemBySlot calls in the overlay renderer to include accessory goggles.
     * 
     * Create's overlay renderer checks if the player has goggles via:
     * player.getItemBySlot(EquipmentSlot.HEAD)
     * 
     * We intercept this to also return goggles from accessory slots if HEAD is empty.
     * This allows the overlay to render when goggles are in an accessory slot.
     * 
     * Note: Using method = "*" is necessary because Create's overlay rendering may
     * happen in multiple methods, and different Create versions/forks may structure
     * the code differently. The slot parameter is captured from the getItemBySlot call.
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
