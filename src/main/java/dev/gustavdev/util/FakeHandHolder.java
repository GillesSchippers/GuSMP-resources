package dev.gustavdev.util;

import net.minecraft.world.InteractionHand;

/**
 * Holds a reference to the fake hand used for checking accessories.
 * This hand is added to the InteractionHand enum to allow the totem system
 * to check accessories without depending on other mods.
 */
public class FakeHandHolder {
    public static InteractionHand FAKE_HAND;
}
