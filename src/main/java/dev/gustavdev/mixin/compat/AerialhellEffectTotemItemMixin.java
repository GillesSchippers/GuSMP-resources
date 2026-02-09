package dev.gustavdev.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.gustavdev.util.AccessoryUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mixin for Aerialhell's EffectTotemItem to support accessory slots.
 * 
 * This mixin modifies the inventoryTick method to also check for totems in accessory slots,
 * allowing Aerialhell's effect totems to work when equipped as accessories.
 * 
 * Note: AerialHell uses Yarn mappings, this project uses Mojang mappings.
 * The mixin is set to remap=false and uses Yarn package names in @At targets.
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
     * 1. Neither main hand nor off hand has the totem, AND
     * 2. An accessory slot has this specific totem
     * 
     * This makes the condition pass when the totem is in an accessory slot.
     */
    @ModifyExpressionValue(
        method = "inventoryTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;"
        )
    )
    private ItemStack checkAccessorySlotForTotem(ItemStack offHandStack, 
                                                  ItemStack totemStack,
                                                  net.minecraft.server.level.ServerLevel world,
                                                  Entity entity) {
        // If entity is a LivingEntity, check accessories
        if (entity instanceof LivingEntity livingEntity) {
            // First check if totem is already in main hand or off hand
            ItemStack mainHandStack = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
            if (mainHandStack.getItem() == totemStack.getItem() || 
                offHandStack.getItem() == totemStack.getItem()) {
                // Already in hand, return original
                return offHandStack;
            }
            
            // Not in hands, check accessory slots for this specific totem
            ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(
                livingEntity,
                itemStack -> itemStack.getItem() == totemStack.getItem()
            );
            
            // If we found this totem in accessories, return it
            // This makes the condition (... || getOffHandStack().getItem() == this) pass
            if (!accessoryTotem.isEmpty()) {
                return accessoryTotem;
            }
        }
        
        // Return the original off hand stack
        return offHandStack;
    }
}

