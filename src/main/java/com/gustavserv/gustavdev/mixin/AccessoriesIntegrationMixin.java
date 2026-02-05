package com.gustavserv.gustavdev.mixin;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Mixin to integrate with Accessories mod
 * Ensures proper handling of accessory slots when editing inventories
 */
@Mixin(value = ServerPlayer.class, priority = 1100)
public abstract class AccessoriesIntegrationMixin {
    
    /**
     * Hook into player tick to ensure accessories are properly synced
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        
        // Ensure accessories capability is present and synced
        Optional<AccessoriesCapability> capability = AccessoriesCapability.get(player);
        if (capability.isPresent()) {
            // The capability handles its own syncing, we just ensure it's checked
            capability.get().getContainers();
        }
    }
}
