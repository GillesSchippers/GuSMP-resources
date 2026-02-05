package com.gustavserv.gustavdev.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin to provide enhanced access to player inventories
 * This mixin ensures proper synchronization when inventory is modified
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerInventoryMixin {
    
    @Shadow
    public abstract Inventory getInventory();
    
    /**
     * Hook to ensure inventory changes are properly synced to client
     */
    public void syncInventory() {
        ServerPlayer player = (ServerPlayer) (Object) this;
        player.containerMenu.broadcastChanges();
    }
}
