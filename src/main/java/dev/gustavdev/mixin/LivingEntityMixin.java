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
 * 2. When getItemInHand(MAIN_HAND/OFF_HAND) is called and hand is empty,
 *    also return the totem from accessories
 *    → Enables passive effects from mods that check hands directly
 * 
 * 3. When setItemInHand(...) is called for any hand with an accessory totem, skip it
 *    → Prevents vanilla from modifying hand slots when totem is in accessory
 * 
 * How it works:
 * - Death protection: Vanilla's checkTotemDeathProtection() loops through InteractionHand.values()
 *   which includes FAKE_HAND, finds the totem, calls shrink(1) to consume it
 * 
 * - Passive effects: Mods like Aerial Hell call getMainHandStack()/getOffHandStack()
 *   on tick, we return the accessory totem if hands are empty, effects are applied
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Wraps getItemInHand to return accessory totems when appropriate.
     * 
     * For FAKE_HAND:
     * - Makes the accessory totem "visible" to vanilla's death protection check
     * 
     * For MAIN_HAND/OFF_HAND when empty:
     * - Makes the accessory totem "visible" to mods checking hands for passive effects
     * - Examples: Aerial Hell totems, Friends and Foes totems with passive abilities
     * 
     * Returns the actual ItemStack reference (not a copy), so when vanilla calls
     * shrink(1) to consume the totem, it modifies the accessory slot directly.
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
        
        // Get the original stack from the hand
        ItemStack handStack = original.call(hand);
        
        // Handle MAIN_HAND and OFF_HAND (for passive effects)
        // Only inject if the hand is empty - don't override actual held items
        if (handStack.isEmpty()) {
            ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isTotem
            );
            if (!accessoryTotem.isEmpty()) {
                return accessoryTotem;
            }
        }
        
        return handStack;
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
        
        // For MAIN_HAND/OFF_HAND: check if we're serving an accessory totem
        // If so, skip the set operation to prevent modifying the hand
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Get what's actually in the hand
        ItemStack actualHandStack;
        if (hand == InteractionHand.MAIN_HAND) {
            actualHandStack = entity.getMainHandItem();
        } else {
            actualHandStack = entity.getOffhandItem();
        }
        
        // If the hand is actually empty but we have an accessory totem, skip
        if (actualHandStack.isEmpty()) {
            ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(entity, GameplayUtil::isTotem);
            if (!accessoryTotem.isEmpty()) {
                // The totem is already modified in the accessory slot
                // Don't let vanilla try to set the hand slot
                return;
            }
        }
        
        // Otherwise, call the original method
        original.call(hand, stack);
    }
}
