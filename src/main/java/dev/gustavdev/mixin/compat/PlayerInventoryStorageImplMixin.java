package dev.gustavdev.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.gustavdev.util.FakeHandHolder;
import dev.gustavdev.util.FakeHeadHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Mixin to prevent Fabric API's PlayerInventoryStorage from trying to use FAKE_HAND and FAKE_HEAD.
 * This prevents crashes and unexpected behavior when Fabric API iterates through hands and equipment slots.
 * 
 * The Fabric Transfer API's PlayerInventoryStorageImpl wraps player inventory for item transfer operations.
 * It iterates through inventory slots including hands and equipment. We need to filter out our fake slots
 * to prevent Fabric API from trying to access them, which would cause errors since they're not backed by
 * actual inventory slots.
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

    /**
     * Removes FAKE_HEAD from the EquipmentSlot array when Fabric API iterates through
     * equipment slots. This prevents Fabric API from trying to access FAKE_HEAD
     * which would cause issues since it's not a real equipment slot.
     * 
     * Uses wildcard method matching (*) to catch all methods in PlayerInventoryStorageImpl
     * that might iterate through equipment slots, as the Fabric API may change implementation
     * details between versions.
     */
    @ModifyExpressionValue(
        method = "*",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EquipmentSlot;values()[Lnet/minecraft/world/entity/EquipmentSlot;"
        )
    )
    private EquipmentSlot[] skipFakeHead(EquipmentSlot[] original) {
        // Remove FAKE_HEAD from the array to prevent Fabric API from using it
        ArrayList<EquipmentSlot> list = new ArrayList<>(Arrays.asList(original));
        list.remove(FakeHeadHolder.FAKE_HEAD);
        return list.toArray(new EquipmentSlot[0]);
    }
}
