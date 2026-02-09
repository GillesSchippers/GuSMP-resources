package dev.gustavdev.mixin;

import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.FakeHandHolder;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Unique
    private boolean gustavdev$checkingActualHand = false;

    /**
     * Returns the totem from accessory slot when queried.
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
    @Inject(method = "getItemInHand", at = @At("RETURN"), cancellable = true)
    private void injectAccessoryTotem(InteractionHand hand, CallbackInfoReturnable<ItemStack> cir) {
        // Prevent recursion when we check actual hand contents in setItemInHand
        if (gustavdev$checkingActualHand) {
            return;
        }
        
        // Handle FAKE_HAND (for death protection)
        if (hand == FakeHandHolder.FAKE_HAND) {
            ItemStack totemStack = AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isTotem
            );
            cir.setReturnValue(totemStack);
            return;
        }
        
        // Handle MAIN_HAND and OFF_HAND (for passive effects)
        // Only inject if the hand is empty - don't override actual held items
        ItemStack handStack = cir.getReturnValue();
        if (handStack.isEmpty()) {
            ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isTotem
            );
            if (!accessoryTotem.isEmpty()) {
                cir.setReturnValue(accessoryTotem);
            }
        }
    }

    /**
     * Skips setItemInHand for FAKE_HAND and for hands with accessory totems.
     * 
     * When vanilla/mods consume a totem, they call setItemInHand() after shrinking the stack.
     * Since we returned the actual accessory stack reference in getItemInHand(),
     * the totem is already consumed/modified in the accessory slot.
     * 
     * We skip the operation to prevent vanilla from trying to modify the hand slot.
     */
    @Inject(method = "setItemInHand", at = @At("HEAD"), cancellable = true)
    private void skipAccessoryTotemSet(InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        // Always skip FAKE_HAND - it's not a real slot
        if (hand == FakeHandHolder.FAKE_HAND) {
            ci.cancel();
            return;
        }
        
        // For MAIN_HAND/OFF_HAND: if we're serving an accessory totem, skip the set operation
        // This prevents vanilla from modifying the hand when the totem is actually in accessories
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Check what's actually in the hand (not what we're returning via our injection)
        // Use recursion guard to get the real hand contents
        gustavdev$checkingActualHand = true;
        ItemStack actualHandStack;
        if (hand == InteractionHand.MAIN_HAND) {
            actualHandStack = entity.getMainHandItem();
        } else {
            actualHandStack = entity.getOffhandItem();
        }
        gustavdev$checkingActualHand = false;
        
        // If the hand is actually empty but we have an accessory totem
        if (actualHandStack.isEmpty()) {
            ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(entity, GameplayUtil::isTotem);
            if (!accessoryTotem.isEmpty()) {
                // The totem is already modified in the accessory slot
                // Don't let vanilla try to set the hand slot
                ci.cancel();
            }
        }
    }
}
