package dev.gustavdev.mixin;

import dev.gustavdev.util.FakeHandHolder;
import net.minecraft.world.InteractionHand;
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
 * Adds a fake hand to the InteractionHand enum.
 * This fake hand is used to check for totems in accessory slots
 * without requiring external mod dependencies.
 */
@Mixin(InteractionHand.class)
public class InteractionHandMixin {

    @Shadow @Final @Mutable private static InteractionHand[] $VALUES;

    /**
     * Mixin accessor for the InteractionHand enum constructor.
     * This method is replaced at runtime by Mixin and never actually executes the throw statement.
     */
    @Invoker("<init>")
    private static InteractionHand invokeInit(String name, int id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Inject into the static initializer to add FAKE_HAND after the enum constants are initialized.
     * This ensures proper initialization order and avoids early class loading issues.
     */
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addFakeHand(CallbackInfo ci) {
        // Add FAKE_HAND to the InteractionHand enum
        ArrayList<InteractionHand> list = new ArrayList<>(Arrays.asList($VALUES));
        int size = list.size();
        FakeHandHolder.FAKE_HAND = invokeInit("FAKE_HAND", size);
        list.add(FakeHandHolder.FAKE_HAND);
        $VALUES = list.toArray(new InteractionHand[size + 1]);
    }
}
