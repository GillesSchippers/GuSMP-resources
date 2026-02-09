package dev.gustavdev.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mixin for AerialHell's EffectTotemItem to support accessory slots.
 * 
 * IMPORTANT: This mixin ONLY affects AerialHell's totem items. It does not modify
 * vanilla totem behavior or other mods' totem implementations.
 * 
 * OPTIONAL MIXIN: This mixin is marked as optional in gustavdev.mixins.json.
 * If AerialHell is not installed at runtime, this mixin will not apply and will
 * only produce a warning in the logs. The mod will continue to function normally
 * without this mixin.
 * 
 * This mixin enables triple wielding of AerialHell totems:
 * - Main hand: TotemA (original behavior)
 * - Off hand: TotemB (original behavior)
 * - Accessory slot: TotemC (added by this mixin)
 * 
 * AerialHell's original implementation already supports dual wielding different totems
 * in main and off hand. This mixin extends that to allow a third totem in an accessory slot.
 * 
 * Technical details:
 * - AerialHell uses Yarn mappings, this project uses Mojang mappings
 * - The mixin is set to remap=false and uses Yarn package names in @At targets
 * - Only validated totem items (via GameplayUtil.isTotem) are returned from accessories
 */
@Mixin(targets = "fr.factionbedrock.aerialhell.Item.EffectTotemItem", remap = false)
public abstract class AerialhellEffectTotemItemMixin {

    /**
     * Modifies the hand item check to also include accessories.
     * 
     * Original condition in EffectTotemItem:
     * livingEntityIn.getMainHandStack().getItem() == this || livingEntityIn.getOffHandStack().getItem() == this
     * 
     * We intercept the result of getOffHandStack() and replace it with the accessory totem if:
     * 1. Neither main hand nor off hand has THIS specific totem, AND
     * 2. An accessory slot has THIS specific totem, AND
     * 3. The accessory item is validated as a totem via GameplayUtil.isTotem()
     * 
     * This makes the condition pass when the totem is in an accessory slot,
     * enabling triple wielding while preserving dual wielding behavior.
     */
    @ModifyExpressionValue(
        method = "inventoryTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;"
        )
    )
    private ItemStack checkAccessorySlotForTotem(ItemStack offHandStack, 
                                                  ItemStack inventoryStack,
                                                  net.minecraft.server.level.ServerLevel world,
                                                  Entity entity) {
        // Only process for LivingEntity
        if (entity instanceof LivingEntity livingEntity) {
            // Check if this specific totem is already in main hand or off hand
            ItemStack mainHandStack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
            if (mainHandStack.getItem() == inventoryStack.getItem() || 
                offHandStack.getItem() == inventoryStack.getItem()) {
                // This totem is already in a hand, return original off hand
                return offHandStack;
            }
            
            // This totem is not in hands, check accessory slots for it
            ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(
                livingEntity,
                itemStack -> itemStack.getItem() == inventoryStack.getItem() && GameplayUtil.isTotem(itemStack)
            );
            
            // If we found this totem in accessories AND it's validated as a totem,
            // return it so the condition (... || getOffHandStack().getItem() == this) passes
            if (!accessoryTotem.isEmpty()) {
                return accessoryTotem;
            }
        }
        
        // Return the original off hand stack
        return offHandStack;
    }
}

