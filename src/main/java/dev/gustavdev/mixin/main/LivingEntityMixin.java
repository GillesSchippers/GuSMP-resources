package dev.gustavdev.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.FakeHandHolder;
import dev.gustavdev.util.FakeHeadHolder;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to handle FAKE_HAND and FAKE_HEAD interactions with LivingEntity methods.
 * 
 * Implementation based on Accessorify mod by pajicadvance:
 * https://github.com/pajicadvance/accessorify
 * 
 * The approach for FAKE_HAND:
 * 1. When getItemInHand(FAKE_HAND) is called, return the totem from accessories
 *    → Enables death protection via vanilla's InteractionHand.values() loop
 * 
 * 2. When setItemInHand(FAKE_HAND, ...) is called, skip it (not a real slot)
 *    → Prevents vanilla from trying to modify a fake hand slot
 * 
 * The approach for FAKE_HEAD:
 * 1. When getItemBySlot(FAKE_HEAD) is called, return the goggles from accessories
 *    → Enables other mods/systems to check for goggles in accessory slots
 * 
 * 2. When setItemSlot(FAKE_HEAD, ...) is called, skip it (not a real slot)
 *    → Prevents vanilla from trying to modify a fake head slot
 * 
 * How it works:
 * - Death protection: Vanilla's checkTotemDeathProtection() loops through InteractionHand.values()
 *   which includes FAKE_HAND, finds the totem, calls shrink(1) to consume it
 * - Goggles detection: Other mods can check EquipmentSlot.values() for items
 *   which includes FAKE_HEAD, allowing them to find goggles in accessory slots
 * 
 * Note: We do NOT override MAIN_HAND/OFF_HAND or real equipment slots to preserve 
 * compatibility with other mods and vanilla mechanics.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Wraps getItemInHand to return accessory totems for FAKE_HAND only.
     * 
     * For FAKE_HAND:
     * - Makes the accessory totem "visible" to vanilla's death protection check
     * - Vanilla loops through InteractionHand.values(), which now includes FAKE_HAND
     * - When it calls getItemInHand(FAKE_HAND), we return the totem from accessories
     * 
     * For MAIN_HAND/OFF_HAND:
     * - Returns the actual hand contents (not overridden)
     * - This preserves compatibility with other mods and vanilla mechanics
     * - Other mods can see what's actually in the player's hands
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
        
        // For MAIN_HAND and OFF_HAND, return actual hand contents
        // This preserves compatibility - other mods see real items
        return original.call(hand);
    }

    /**
     * Wraps setItemInHand to skip operations for FAKE_HAND only.
     * 
     * When vanilla consumes a totem via death protection, it calls setItemInHand(FAKE_HAND, ...)
     * after shrinking the stack. Since FAKE_HAND is not a real slot, we skip this operation.
     * 
     * The totem is already consumed in the accessory slot (we returned a direct reference),
     * so no further action is needed.
     */
    @WrapMethod(method = "setItemInHand")
    private void wrapSetItemInHand(InteractionHand hand, ItemStack stack, Operation<Void> original) {
        // Skip FAKE_HAND - it's not a real slot
        if (hand == FakeHandHolder.FAKE_HAND) {
            return;
        }
        
        // For MAIN_HAND and OFF_HAND, call original (normal behavior)
        original.call(hand, stack);
    }

    /**
     * Wraps getItemBySlot to return accessory items for FAKE_HEAD only.
     * 
     * For FAKE_HEAD:
     * - Makes the accessory goggles "visible" to other mods checking equipment slots
     * - When EquipmentSlot.values() is looped through, FAKE_HEAD is included
     * - When getItemBySlot(FAKE_HEAD) is called, we return goggles from accessories
     * 
     * For real equipment slots (HEAD, CHEST, LEGS, FEET):
     * - Returns the actual equipment contents (not overridden)
     * - This preserves compatibility with other mods and vanilla mechanics
     * - Other mods can see what's actually equipped
     * 
     * Returns the actual ItemStack reference (not a copy), so modifications
     * affect the accessory slot directly.
     */
    @WrapMethod(method = "getItemBySlot")
    private ItemStack wrapGetItemBySlot(EquipmentSlot slot, Operation<ItemStack> original) {
        // Handle FAKE_HEAD (for goggles in accessories)
        if (slot == FakeHeadHolder.FAKE_HEAD) {
            return AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isGoggles
            );
        }
        
        // For real equipment slots, return actual equipment contents
        return original.call(slot);
    }

    /**
     * Wraps setItemSlot to skip operations for FAKE_HEAD only.
     * 
     * When other code tries to set an item in FAKE_HEAD, we skip this operation
     * since FAKE_HEAD is not a real slot. The accessory slot is managed by the
     * accessories system.
     */
    @WrapMethod(method = "setItemSlot")
    private void wrapSetItemSlot(EquipmentSlot slot, ItemStack stack, Operation<Void> original) {
        // Skip FAKE_HEAD - it's not a real slot
        if (slot == FakeHeadHolder.FAKE_HEAD) {
            return;
        }
        
        // For real equipment slots, call original (normal behavior)
        original.call(slot, stack);
    }
}
