package dev.gustavdev.mixin.fake;

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
@Mixin(value = InteractionHand.class, priority = 2000)
public class InteractionHandMixin {

    @Shadow @Final @Mutable private static InteractionHand[] $VALUES;

    /**
     * Mixin accessor for the InteractionHand enum constructor.
     * This method is replaced at runtime by Mixin and never actually executes the throw statement.
     */
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
