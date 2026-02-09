package dev.gustavdev.mixin;

import dev.gustavdev.util.FakeHeadHolder;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Adds a fake head slot to the EquipmentSlot enum.
 * This fake head is used to check for goggles in accessory slots
 * without requiring external mod dependencies.
 */
@Mixin(EquipmentSlot.class)
public class EquipmentSlotMixin {

    @Shadow @Final @Mutable private static EquipmentSlot[] $VALUES;

    /**
     * Mixin accessor for the EquipmentSlot enum constructor.
     * This method is replaced at runtime by Mixin and never actually executes the throw statement.
     * 
     * EquipmentSlot constructor signature (Mojang mappings):
     * EquipmentSlot(String name, int ordinal, Type type, int index, int filterFlag, String serializedName)
     */
    @Invoker("<init>")
    private static EquipmentSlot invokeInit(String name, int ordinal, EquipmentSlot.Type type, int index, int filterFlag, String serializedName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Inject into the static initializer to add FAKE_HEAD after the enum constants are initialized.
     * This ensures EquipmentSlot.Type is fully initialized before we access it.
     */
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addFakeHead(CallbackInfo ci) {
        // Add FAKE_HEAD to the EquipmentSlot enum
        ArrayList<EquipmentSlot> list = new ArrayList<>(Arrays.asList($VALUES));
        int size = list.size();
        // Create FAKE_HEAD with HUMANOID_ARMOR type, index 0 (corresponds to head slot), 
        // unique filter flag (1 << size), and serialized name "fake_head"
        FakeHeadHolder.FAKE_HEAD = invokeInit("FAKE_HEAD", size, EquipmentSlot.Type.HUMANOID_ARMOR, 0, 1 << size, "fake_head");
        list.add(FakeHeadHolder.FAKE_HEAD);
        $VALUES = list.toArray(new EquipmentSlot[size + 1]);
    }
}
