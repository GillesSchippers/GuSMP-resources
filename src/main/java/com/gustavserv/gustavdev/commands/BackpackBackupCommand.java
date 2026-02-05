package com.gustavserv.gustavdev.commands;

import com.gustavserv.gustavdev.util.BackpackHelper;
import com.gustavserv.gustavdev.util.BackupManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.List;

public class BackpackBackupCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("backpack")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            
            // /backpack list <player>
            .then(Commands.literal("list")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> listPlayerBackpacks(ctx))
                )
            )
            
            // /backpack backup <player> <backpackIndex> <name>
            .then(Commands.literal("backup")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("backpackIndex", IntegerArgumentType.integer(0))
                        .then(Commands.argument("name", StringArgumentType.string())
                            .executes(ctx -> backupBackpack(ctx))
                        )
                    )
                )
            )
            
            // /backpack restore <player> <backupFileName>
            .then(Commands.literal("restore")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("backupFileName", StringArgumentType.greedyString())
                        .executes(ctx -> restoreBackpack(ctx))
                    )
                )
            )
            
            // /backpack listbackups [player]
            .then(Commands.literal("listbackups")
                .executes(ctx -> listAllBackups(ctx))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> listPlayerBackups(ctx))
                )
            )
            
            // /backpack delete <backupFileName>
            .then(Commands.literal("delete")
                .then(Commands.argument("backupFileName", StringArgumentType.greedyString())
                    .executes(ctx -> deleteBackup(ctx))
                )
            )
            
            // /backpack clear <player> <backpackIndex>
            .then(Commands.literal("clear")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("backpackIndex", IntegerArgumentType.integer(0))
                        .executes(ctx -> clearBackpack(ctx))
                    )
                )
            )
        );
    }
    
    private static int listPlayerBackpacks(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        
        List<ItemStack> inventoryBackpacks = BackpackHelper.findBackpacksInInventory(target.getInventory());
        List<ItemStack> accessoriesBackpacks = BackpackHelper.findBackpacksInAccessories(target);
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "=== " + target.getName().getString() + "'s Backpacks ==="
        ), false);
        
        int index = 0;
        
        if (!inventoryBackpacks.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Inventory Backpacks:"), false);
            for (ItemStack backpack : inventoryBackpacks) {
                int finalIndex = index++;
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "  [" + finalIndex + "] " + backpack.getDisplayName().getString()
                ), false);
            }
        }
        
        if (!accessoriesBackpacks.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Accessories Backpacks:"), false);
            for (ItemStack backpack : accessoriesBackpacks) {
                int finalIndex = index++;
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "  [" + finalIndex + "] " + backpack.getDisplayName().getString()
                ), false);
            }
        }
        
        if (inventoryBackpacks.isEmpty() && accessoriesBackpacks.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                target.getName().getString() + " has no backpacks"
            ), false);
        }
        
        return 1;
    }
    
    private static int backupBackpack(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int backpackIndex = IntegerArgumentType.getInteger(ctx, "backpackIndex");
        String backupName = StringArgumentType.getString(ctx, "name");
        
        ItemStack backpack = BackpackHelper.findBackpackByIndex(target, backpackIndex);
        
        if (backpack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                "No backpack found at index " + backpackIndex
            ));
            return 0;
        }
        
        try {
            String fileName = BackupManager.createBackpackBackup(
                ctx.getSource().getServer(),
                target.getUUID(),
                backpack,
                backupName
            );
            
            ctx.getSource().sendSuccess(() -> Component.literal(
                "Created backup: " + fileName
            ), true);
            
            return 1;
        } catch (IOException e) {
            ctx.getSource().sendFailure(Component.literal(
                "Failed to create backup: " + e.getMessage()
            ));
            return 0;
        }
    }
    
    private static int restoreBackpack(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String backupFileName = StringArgumentType.getString(ctx, "backupFileName");
        
        try {
            ItemStack backpack = BackupManager.restoreBackpackBackup(
                ctx.getSource().getServer(),
                backupFileName
            );
            
            if (backpack.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal(
                    "Failed to restore backpack from backup"
                ));
                return 0;
            }
            
            // Try to find an empty slot in inventory
            boolean added = target.getInventory().add(backpack);
            
            if (added) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "Restored backpack from backup: " + backupFileName + " to " + target.getName().getString() + "'s inventory"
                ), true);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal(
                    "Restored backpack but player's inventory is full. Backpack was not added."
                ));
                return 0;
            }
        } catch (IOException e) {
            ctx.getSource().sendFailure(Component.literal(
                "Failed to restore backup: " + e.getMessage()
            ));
            return 0;
        }
    }
    
    private static int listAllBackups(CommandContext<CommandSourceStack> ctx) {
        List<BackupManager.BackupInfo> backups = BackupManager.listAllBackups(ctx.getSource().getServer());
        
        if (backups.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No backups found"), false);
            return 1;
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal("=== All Backpack Backups ==="), false);
        
        for (BackupManager.BackupInfo backup : backups) {
            ctx.getSource().sendSuccess(() -> Component.literal(backup.toString()), false);
        }
        
        return 1;
    }
    
    private static int listPlayerBackupsOnly(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        
        List<BackupManager.BackupInfo> backups = BackupManager.listPlayerBackups(
            ctx.getSource().getServer(),
            target.getUUID()
        );
        
        if (backups.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "No backups found for " + target.getName().getString()
            ), false);
            return 1;
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "=== Backups for " + target.getName().getString() + " ==="
        ), false);
        
        for (BackupManager.BackupInfo backup : backups) {
            ctx.getSource().sendSuccess(() -> Component.literal(backup.toString()), false);
        }
        
        return 1;
    }
    
    private static int deleteBackup(CommandContext<CommandSourceStack> ctx) {
        String backupFileName = StringArgumentType.getString(ctx, "backupFileName");
        
        boolean success = BackupManager.deleteBackup(ctx.getSource().getServer(), backupFileName);
        
        if (success) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "Deleted backup: " + backupFileName
            ), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal(
                "Failed to delete backup: " + backupFileName
            ));
            return 0;
        }
    }
    
    private static int clearBackpack(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int backpackIndex = IntegerArgumentType.getInteger(ctx, "backpackIndex");
        
        ItemStack backpack = BackpackHelper.findBackpackByIndex(target, backpackIndex);
        
        if (backpack.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                "No backpack found at index " + backpackIndex
            ));
            return 0;
        }
        
        BackpackHelper.clearBackpack(backpack);
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "Cleared backpack at index " + backpackIndex + " for " + target.getName().getString()
        ), true);
        
        return 1;
    }
}
