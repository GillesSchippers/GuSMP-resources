package dev.gustavdev.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.FakeHandHolder;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to handle FAKE_HAND interactions with LivingEntity methods.
 * 
 * Implementation based on Accessorify mod by pajicadvance:
 * https://github.com/pajicadvance/accessorify
 * 
 * Extended to support passive effect totems that check hands on tick
 * (e.g., Aerial Hell totems that apply effects when held).
 * 
 * The approach:
 * 1. When getItemInHand(FAKE_HAND) is called, return the totem from accessories
 *    → Enables death protection via vanilla's InteractionHand.values() loop
 * 
 * 2. When getItemInHand(MAIN_HAND/OFF_HAND) is called, ALWAYS check for accessory totem
 *    and return it if present, REGARDLESS of hand occupancy
 *    → Enables passive effects even when holding other items (sword + shield)
 * 
 * 3. When setItemInHand(...) is called with an accessory totem present, skip it
 *    → Prevents vanilla from modifying hand slots when totem is in accessory
 * 
 * How it works:
 * - Death protection: Vanilla's checkTotemDeathProtection() loops through InteractionHand.values()
 *   which includes FAKE_HAND, finds the totem, calls shrink(1) to consume it
 * 
 * - Passive effects: Mods like Aerial Hell call getMainHandStack()/getOffHandStack() on tick.
 *   We always return the accessory totem if present, so effects apply even when holding items.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Wraps getItemInHand to return accessory totems when appropriate.
     * 
     * For FAKE_HAND:
     * - Makes the accessory totem "visible" to vanilla's death protection check
     * 
     * For MAIN_HAND/OFF_HAND:
     * - ALWAYS returns accessory totem if present, regardless of hand occupancy
     * - This allows passive effects (Aerial Hell) to work even when holding other items
     * - Examples: Holding sword + shield while having totem in accessory slot
     * 
     * Returns the actual ItemStack reference (not a copy), so when vanilla calls
     * shrink(1) to consume the totem, it modifies the accessory slot directly.
     * 
     * NOTE: This means hand-held items won't be accessible while totem is in accessory.
     * This is a trade-off to enable passive effects to work with occupied hands.
     */
    @WrapMethod(method = "getItemInHand")
    private ItemStack wrapGetItemInHand(InteractionHand hand, Operation<ItemStack> original) {
        // Handle FAKE_HAND (for death protection)
        if (hand == FakeHandHolder.FAKE_HAND) {
            return AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isTotem
            );
        }
        
        // Early exit: If no accessory totem, use original logic
        // This avoids unnecessary processing when player doesn't have a totem equipped
        ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(
            (LivingEntity) (Object) this,
            GameplayUtil::isTotem
        );
        if (accessoryTotem.isEmpty()) {
            return original.call(hand);
        }
        
        // Handle MAIN_HAND and OFF_HAND (for passive effects)
        // Return accessory totem REGARDLESS of hand occupancy
        // This allows passive effects to work even when holding other items (sword + shield)
        return accessoryTotem;
    }

    /**
     * Wraps setItemInHand to skip operations for FAKE_HAND and accessory totems.
     * 
     * When vanilla/mods consume a totem, they call setItemInHand() after shrinking the stack.
     * Since we returned the actual accessory stack reference in getItemInHand(),
     * the totem is already consumed/modified in the accessory slot.
     * 
     * We skip the operation to prevent vanilla from trying to modify the hand slot.
     */
    @WrapMethod(method = "setItemInHand")
    private void wrapSetItemInHand(InteractionHand hand, ItemStack stack, Operation<Void> original) {
        // Always skip FAKE_HAND - it's not a real slot
        if (hand == FakeHandHolder.FAKE_HAND) {
            return;
        }
        
        // Early exit: If no accessory totem, use original logic
        // This avoids unnecessary processing when player doesn't have a totem equipped
        LivingEntity entity = (LivingEntity) (Object) this;
        ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(entity, GameplayUtil::isTotem);
        
        if (accessoryTotem.isEmpty()) {
            original.call(hand, stack);
            return;
        }
        
        // For MAIN_HAND/OFF_HAND: we have an accessory totem
        // Skip the set operation since we're always returning the totem
        // and it's already been modified in the accessory slot
        // (Don't call original - the totem is already consumed in accessory)
    }
}
