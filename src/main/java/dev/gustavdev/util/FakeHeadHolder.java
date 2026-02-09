package dev.gustavdev.util;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Holds a reference to the fake head used for checking accessories.
 * This head slot is added to the EquipmentSlot enum to allow the goggles system
 * to check accessories without depending on other mods.
 * 
 * Note: FAKE_HEAD is initialized by EquipmentSlotMixin's static initializer.
 */
public class FakeHeadHolder {
    public static EquipmentSlot FAKE_HEAD;
}
