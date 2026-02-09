package dev.gustavdev.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Mixin for Aerialhell's EffectTotemItem to support accessory slots.
 * 
 * This mixin modifies the inventoryTick method to also check for totems in accessory slots,
 * allowing Aerialhell's effect totems to work when equipped as accessories.
 */
@Mixin(targets = "fr.factionbedrock.aerialhell.Item.EffectTotemItem")
public class AerialhellEffectTotemItemMixin {

    /**
     * Wraps the condition check in inventoryTick to also check for the totem in accessory slots.
     * 
     * Original condition checks:
     * livingEntityIn.getMainHandStack().getItem() == this || livingEntityIn.getOffHandStack().getItem() == this
     * 
     * We intercept the second getItem() call and return 'this' if:
     * 1. The off hand doesn't have the totem (original returns false), AND
     * 2. The totem is found in an accessory slot
     * 
     * This allows the totem effects to trigger when worn as an accessory.
     */
    @WrapOperation(
        method = "inventoryTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
            ordinal = 1
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/LivingEntity;getOffHandStack()Lnet/minecraft/world/item/ItemStack;"
            )
        )
    )
    private Item checkAccessorySlot(ItemStack stack, Operation<Item> original, 
                                    ItemStack totemStack, 
                                    net.minecraft.server.world.ServerWorld world,
                                    net.minecraft.world.entity.Entity entity) {
        // Get the original result (the item in the off hand)
        Item offHandItem = original.call(stack);
        
        // If off hand has the totem, return it
        if (offHandItem == totemStack.getItem()) {
            return offHandItem;
        }
        
        // Otherwise check accessory slots
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(
                livingEntity,
                GameplayUtil::isTotem
            );
            
            // If we found a totem in accessories and it matches this totem, return 'this'
            if (!accessoryTotem.isEmpty() && accessoryTotem.getItem() == totemStack.getItem()) {
                return totemStack.getItem();
            }
        }
        
        // Otherwise return the off hand item (which won't match, so condition fails)
        return offHandItem;
    }
}
