package dev.gustavdev.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.gustavdev.util.FakeHandHolder;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Mixin to prevent Fabric API's PlayerInventoryStorage from trying to use FAKE_HAND.
 * This prevents crashes and unexpected behavior when Fabric API iterates through hands.
 * 
 * The Fabric Transfer API's PlayerInventoryStorageImpl wraps player inventory for item transfer operations.
 * It iterates through inventory slots including hands. We need to filter out our fake hand
 * to prevent Fabric API from trying to access it, which would cause errors since it's not backed by
 * an actual inventory slot.
 */
@Mixin(targets = "net/fabricmc/fabric/impl/transfer/item/PlayerInventoryStorageImpl")
public class PlayerInventoryStorageImplMixin {

    /**
     * Removes FAKE_HAND from the InteractionHand array when Fabric API's offer method
     * iterates through hands. This prevents Fabric API from trying to access FAKE_HAND
     * which would cause issues since it's not a real hand slot.
     */
    @ModifyExpressionValue(
        method = "offer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;"
        )
    )
    private InteractionHand[] skipFakeHand(InteractionHand[] original) {
        // Remove FAKE_HAND from the array to prevent Fabric API from using it
        ArrayList<InteractionHand> list = new ArrayList<>(Arrays.asList(original));
        list.remove(FakeHandHolder.FAKE_HAND);
        return list.toArray(new InteractionHand[0]);
    }
}
