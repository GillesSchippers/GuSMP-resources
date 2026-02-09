package dev.gustavdev.mixin;

import dev.gustavdev.util.AccessoryUtil;
import dev.gustavdev.util.FakeHandHolder;
import dev.gustavdev.util.GameplayUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to handle FAKE_HAND interactions with LivingEntity methods.
 * This prevents crashes when other mods (like Create) call getItemInHand or setItemInHand
 * with the custom FAKE_HAND enum value, and enables totem functionality from accessory slots.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    protected abstract void onEquippedItemBroken(net.minecraft.world.item.Item item, net.minecraft.world.entity.EquipmentSlot slot);

    /**
     * Intercepts getItemInHand calls to handle FAKE_HAND.
     * Returns the totem from accessory slot if available, or empty stack otherwise.
     */
    @Inject(method = "getItemInHand", at = @At("HEAD"), cancellable = true)
    private void handleFakeHandGet(InteractionHand hand, CallbackInfoReturnable<ItemStack> cir) {
        if (hand == FakeHandHolder.FAKE_HAND) {
            // Check for totem in accessory slot
            ItemStack totemStack = AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isTotem
            );
            cir.setReturnValue(totemStack);
        }
    }

    /**
     * Intercepts setItemInHand calls to handle FAKE_HAND.
     * For FAKE_HAND, we need to remove the totem from the accessory slot instead.
     */
    @Inject(method = "setItemInHand", at = @At("HEAD"), cancellable = true)
    private void handleFakeHandSet(InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        if (hand == FakeHandHolder.FAKE_HAND) {
            // When vanilla code tries to set FAKE_HAND to empty (consuming the totem),
            // we need to actually remove it from the accessory slot
            if (stack.isEmpty()) {
                LivingEntity entity = (LivingEntity) (Object) this;
                // Get the totem from accessory
                ItemStack totemStack = AccessoryUtil.getAccessoryStack(entity, GameplayUtil::isTotem);
                if (!totemStack.isEmpty()) {
                    // Shrink the totem stack (consume it)
                    totemStack.shrink(1);
                }
            }
            // Cancel the vanilla operation
            ci.cancel();
        }
    }

    /**
     * Checks for totem in accessory slot when vanilla totem check fails.
     * This allows totems in accessory slots to work.
     */
    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"), cancellable = true)
    private void checkAccessoryTotem(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        // Only check if vanilla didn't find a totem (return value is false)
        if (!cir.getReturnValue()) {
            LivingEntity entity = (LivingEntity) (Object) this;
            
            // Check if there's a totem in the accessory slot using FAKE_HAND
            ItemStack totemStack = entity.getItemInHand(FakeHandHolder.FAKE_HAND);
            
            if (!totemStack.isEmpty() && GameplayUtil.isTotem(totemStack)) {
                // Found totem in accessory, manually consume it and trigger effects
                entity.setItemInHand(FakeHandHolder.FAKE_HAND, ItemStack.EMPTY);
                cir.setReturnValue(true);
            }
        }
    }
}
