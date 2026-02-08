package dev.gustavdev.mixin;

import dev.gustavdev.util.FakeHandHolder;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Adds a fake hand to the InteractionHand enum.
 * This fake hand is used to check for totems in accessory slots
 * without requiring external mod dependencies.
 */
@Mixin(InteractionHand.class)
public class InteractionHandMixin {

    @Shadow @Final @Mutable private static InteractionHand[] $VALUES;

    @Invoker("<init>")
    static InteractionHand invokeInit(String name, int id) {
        throw new UnsupportedOperationException();
    }

    static {
        // Add FAKE_HAND to the InteractionHand enum
        ArrayList<InteractionHand> list = new ArrayList<>(Arrays.asList($VALUES));
        int size = list.size();
        FakeHandHolder.FAKE_HAND = invokeInit("FAKE_HAND", size);
        list.add(FakeHandHolder.FAKE_HAND);
        $VALUES = list.toArray(new InteractionHand[size + 1]);
    }
}
