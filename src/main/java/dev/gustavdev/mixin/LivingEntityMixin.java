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
 * This prevents crashes when other mods (like Create) call getItemInHand or setItemInHand
 * with the custom FAKE_HAND enum value, and enables totem functionality from accessory slots.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private boolean gustavdev$isGettingHand = false;

    /**
     * Intercepts getItemInHand calls to handle FAKE_HAND and inject accessory totems.
     * For FAKE_HAND: Returns the totem from accessory slot if available.
     * For MAIN_HAND/OFF_HAND: If the hand is empty, checks accessories and returns totem if found.
     * This allows vanilla totem logic to work with accessories by making them appear as if in hand.
     * This also enables passive totem effects to work from accessory slot.
     */
    @Inject(method = "getItemInHand", at = @At("RETURN"), cancellable = true)
    private void injectAccessoryTotem(InteractionHand hand, CallbackInfoReturnable<ItemStack> cir) {
        // Prevent recursion
        if (gustavdev$isGettingHand) {
            return;
        }
        
        // Handle FAKE_HAND specially
        if (hand == FakeHandHolder.FAKE_HAND) {
            ItemStack totemStack = AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isTotem
            );
            cir.setReturnValue(totemStack);
            return;
        }
        
        // For MAIN_HAND and OFF_HAND: if hand is empty, check accessories
        // This makes accessories appear as if held in hand for totem checks and passive effects
        ItemStack handStack = cir.getReturnValue();
        if (handStack.isEmpty()) {
            ItemStack accessoryStack = AccessoryUtil.getAccessoryStack(
                (LivingEntity) (Object) this,
                GameplayUtil::isTotem
            );
            if (!accessoryStack.isEmpty()) {
                cir.setReturnValue(accessoryStack);
            }
        }
    }

    /**
     * Intercepts setItemInHand calls to handle totem consumption from accessories.
     * When vanilla tries to consume a totem from hand, but the totem is actually in
     * an accessory slot (we made it appear in hand via getItemInHand), we need to
     * consume it from the accessory slot instead.
     */
    @Inject(method = "setItemInHand", at = @At("HEAD"), cancellable = true)
    private void handleAccessoryTotemConsumption(InteractionHand hand, ItemStack newStack, CallbackInfo ci) {
        if (hand == FakeHandHolder.FAKE_HAND) {
            // FAKE_HAND is purely virtual, handle consumption in accessory slot
            if (newStack.isEmpty()) {
                LivingEntity entity = (LivingEntity) (Object) this;
                ItemStack totemStack = AccessoryUtil.getAccessoryStack(entity, GameplayUtil::isTotem);
                if (!totemStack.isEmpty()) {
                    totemStack.shrink(1);
                }
            }
            ci.cancel();
            return;
        }
        
        // For MAIN_HAND/OFF_HAND: Check if we're trying to consume a totem
        // Get the actual hand stack (without our injection)
        LivingEntity entity = (LivingEntity) (Object) this;
        gustavdev$isGettingHand = true;
        ItemStack actualHandStack = entity.getItemInHand(hand);
        gustavdev$isGettingHand = false;
        
        // If the actual hand is empty, but we have an accessory totem
        if (actualHandStack.isEmpty()) {
            ItemStack accessoryTotem = AccessoryUtil.getAccessoryStack(entity, GameplayUtil::isTotem);
            if (!accessoryTotem.isEmpty() && newStack.isEmpty()) {
                // Vanilla is trying to consume the totem (set to empty)
                // Consume it from the accessory instead
                accessoryTotem.shrink(1);
                ci.cancel();
            }
        }
    }
}
