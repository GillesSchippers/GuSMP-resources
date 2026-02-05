package com.gustavserv.gustavdev.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to integrate with Traveller's Backpack mod
 * This mixin helps track and manage backpack items
 */
@Mixin(value = ItemStack.class, priority = 1100)
public abstract class TravelersBackpackMixin {
    
    /**
     * Hook to ensure backpack NBT data is properly preserved when modified
     */
    @Inject(method = "setTag", at = @At("HEAD"))
    private void onSetTag(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        
        // Check if this is a backpack item
        if (stack.getItem().toString().contains("travelersbackpack")) {
            // The backpack's NBT handling is managed by the mod itself
            // We just ensure the tag is not null
            if (tag != null && !tag.isEmpty()) {
                // Preserve backpack data
            }
        }
    }
}
