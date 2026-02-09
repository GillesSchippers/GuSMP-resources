package dev.gustavdev.mixin;

import dev.gustavdev.util.FakeHeadHolder;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Adds a fake head slot to the EquipmentSlot enum.
 * This fake head is used to check for goggles in accessory slots
 * without requiring external mod dependencies.
 */
@Mixin(value = EquipmentSlot.class, priority = 2000)
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
    static EquipmentSlot invokeInit(String name, int ordinal, EquipmentSlot.Type type, int index, int filterFlag, String serializedName) {
        throw new UnsupportedOperationException();
    }

    static {
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
