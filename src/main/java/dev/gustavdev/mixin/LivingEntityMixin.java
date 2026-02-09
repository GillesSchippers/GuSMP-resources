package dev.gustavdev.mixin;

import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.FakeHandHolder;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
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
 * The approach is simple:
 * 1. When getItemInHand(FAKE_HAND) is called, return the totem from accessories
 * 2. When setItemInHand(FAKE_HAND, ...) is called, skip it (FAKE_HAND is not a real slot)
 * 3. Vanilla/mods handle everything else (death protection, consumption, passive effects)
 * 
 * How it works:
 * - Vanilla's checkTotemDeathProtection() loops through InteractionHand.values()
 * - Since FAKE_HAND is now in that array, vanilla will call getItemInHand(FAKE_HAND)
 * - We return the actual totem stack from accessories
 * - Vanilla calls shrink(1) on that stack to consume it (modifies accessories directly)
 * - Vanilla calls setItemInHand(FAKE_HAND, ...) which we skip
 * - All vanilla totem effects (particles, sound, healing) work automatically
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Returns the totem from accessory slot when FAKE_HAND is queried.
     * 
     * This makes the accessory totem "visible" to vanilla's death protection check
     * and to any mods that iterate through InteractionHand.values() to check for items.
     * 
     * Returns the actual ItemStack reference (not a copy), so when vanilla calls
     * shrink(1) to consume the totem, it modifies the accessory slot directly.
     */
    @Inject(method = "getItemInHand", at = @At("HEAD"), cancellable = true)
    private void checkFakeHandForTotem(InteractionHand hand, CallbackInfoReturnable<ItemStack> cir) {
        if (hand == FakeHandHolder.FAKE_HAND) {
            ItemStack totemStack = AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isTotem
            );
            cir.setReturnValue(totemStack);
        }
    }

    /**
     * Skips setItemInHand for FAKE_HAND.
     * 
     * After vanilla consumes the totem via shrink(1), it tries to call
     * setItemInHand(FAKE_HAND, modifiedStack). Since FAKE_HAND is not a real slot
     * and the totem is already consumed in the accessory, we just skip this operation.
     */
    @Inject(method = "setItemInHand", at = @At("HEAD"), cancellable = true)
    private void skipFakeHand(InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        if (hand == FakeHandHolder.FAKE_HAND) {
            ci.cancel();
        }
    }
}
