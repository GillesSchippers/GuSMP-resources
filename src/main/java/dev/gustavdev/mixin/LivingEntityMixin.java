package dev.gustavdev.mixin;

import dev.gustavdev.util.FakeHandHolder;
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
 * This prevents crashes when other mods (like Create) call getStackInHand or setItemInHand
 * with the custom FAKE_HAND enum value.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Intercepts getStackInHand calls to handle FAKE_HAND.
     * Returns empty ItemStack for FAKE_HAND to prevent IllegalArgumentException.
     */
    @Inject(method = "getItemInHand", at = @At("HEAD"), cancellable = true)
    private void handleFakeHandGet(InteractionHand hand, CallbackInfoReturnable<ItemStack> cir) {
        if (hand == FakeHandHolder.FAKE_HAND) {
            // Return empty stack for fake hand to prevent crashes
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    /**
     * Intercepts setItemInHand calls to handle FAKE_HAND.
     * Cancels the operation for FAKE_HAND to prevent IllegalArgumentException.
     */
    @Inject(method = "setItemInHand", at = @At("HEAD"), cancellable = true)
    private void handleFakeHandSet(InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        if (hand == FakeHandHolder.FAKE_HAND) {
            // Cancel operation for fake hand to prevent crashes
            ci.cancel();
        }
    }
}
