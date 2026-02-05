package com.gustavserv.gustavdev.commands;

import com.gustavserv.gustavdev.util.AccessoriesHelper;
import com.gustavserv.gustavdev.util.InventoryManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class EditInventoryCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("editinventory")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            
            // /editinventory player <player> set <slot> <item>
            .then(Commands.literal("player")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.literal("set")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(0))
                            .then(Commands.argument("item", ItemArgument.item())
                                .executes(ctx -> setPlayerInventorySlot(ctx))
                            )
                        )
                    )
                    .then(Commands.literal("get")
                        .then(Commands.argument("slot", IntegerArgumentType.integer(0))
                            .executes(ctx -> getPlayerInventorySlot(ctx))
                        )
                    )
                    .then(Commands.literal("clear")
                        .executes(ctx -> clearPlayerInventory(ctx))
                    )
                    .then(Commands.literal("list")
                        .executes(ctx -> listPlayerInventory(ctx))
                    )
                )
            )
            
            // /editinventory accessories <player> list
            .then(Commands.literal("accessories")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.literal("list")
                        .executes(ctx -> listPlayerAccessories(ctx))
                    )
                    .then(Commands.literal("set")
                        .then(Commands.argument("slotType", StringArgumentType.string())
                            .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                .then(Commands.argument("item", ItemArgument.item())
                                    .executes(ctx -> setAccessorySlot(ctx))
                                )
                            )
                        )
                    )
                    .then(Commands.literal("get")
                        .then(Commands.argument("slotType", StringArgumentType.string())
                            .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                .executes(ctx -> getAccessorySlot(ctx))
                            )
                        )
                    )
                    .then(Commands.literal("clear")
                        .executes(ctx -> clearPlayerAccessories(ctx))
                    )
                )
            )
        );
    }
    
    private static int setPlayerInventorySlot(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int slot = IntegerArgumentType.getInteger(ctx, "slot");
        ItemInput itemInput = ItemArgument.getItem(ctx, "item");
        ItemStack item = itemInput.createItemStack(1, false);
        
        Inventory inventory = target.getInventory();
        
        if (slot < 0 || slot >= inventory.getContainerSize()) {
            ctx.getSource().sendFailure(Component.literal("Invalid slot number. Must be between 0 and " + (inventory.getContainerSize() - 1)));
            return 0;
        }
        
        InventoryManager.setInventorySlot(inventory, slot, item);
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "Set slot " + slot + " in " + target.getName().getString() + "'s inventory to " + item.getDisplayName().getString()
        ), true);
        
        return 1;
    }
    
    private static int getPlayerInventorySlot(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int slot = IntegerArgumentType.getInteger(ctx, "slot");
        
        Inventory inventory = target.getInventory();
        
        if (slot < 0 || slot >= inventory.getContainerSize()) {
            ctx.getSource().sendFailure(Component.literal("Invalid slot number. Must be between 0 and " + (inventory.getContainerSize() - 1)));
            return 0;
        }
        
        ItemStack item = InventoryManager.getInventorySlot(inventory, slot);
        
        if (item.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "Slot " + slot + " in " + target.getName().getString() + "'s inventory is empty"
            ), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "Slot " + slot + " in " + target.getName().getString() + "'s inventory: " + 
                item.getCount() + "x " + item.getDisplayName().getString()
            ), false);
        }
        
        return 1;
    }
    
    private static int clearPlayerInventory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        
        InventoryManager.clearInventory(target.getInventory());
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "Cleared " + target.getName().getString() + "'s inventory"
        ), true);
        
        return 1;
    }
    
    private static int listPlayerInventory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        Inventory inventory = target.getInventory();
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "=== " + target.getName().getString() + "'s Inventory ==="
        ), false);
        
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                int finalI = i;
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "Slot " + finalI + ": " + item.getCount() + "x " + item.getDisplayName().getString()
                ), false);
            }
        }
        
        return 1;
    }
    
    private static int listPlayerAccessories(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        
        Map<String, List<ItemStack>> accessories = AccessoriesHelper.getPlayerAccessories(target);
        
        if (accessories.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                target.getName().getString() + " has no accessories equipped"
            ), false);
            return 1;
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "=== " + target.getName().getString() + "'s Accessories ==="
        ), false);
        
        for (Map.Entry<String, List<ItemStack>> entry : accessories.entrySet()) {
            String slotType = entry.getKey();
            List<ItemStack> items = entry.getValue();
            
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                int finalI = i;
                ctx.getSource().sendSuccess(() -> Component.literal(
                    slotType + "[" + finalI + "]: " + item.getCount() + "x " + item.getDisplayName().getString()
                ), false);
            }
        }
        
        return 1;
    }
    
    private static int setAccessorySlot(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String slotType = StringArgumentType.getString(ctx, "slotType");
        int index = IntegerArgumentType.getInteger(ctx, "index");
        ItemInput itemInput = ItemArgument.getItem(ctx, "item");
        ItemStack item = itemInput.createItemStack(1, false);
        
        boolean success = AccessoriesHelper.setAccessorySlot(target, slotType, index, item);
        
        if (success) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "Set " + slotType + "[" + index + "] in " + target.getName().getString() + 
                "'s accessories to " + item.getDisplayName().getString()
            ), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal(
                "Failed to set accessory slot. Invalid slot type or index."
            ));
            return 0;
        }
    }
    
    private static int getAccessorySlot(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String slotType = StringArgumentType.getString(ctx, "slotType");
        int index = IntegerArgumentType.getInteger(ctx, "index");
        
        ItemStack item = AccessoriesHelper.getAccessorySlot(target, slotType, index);
        
        if (item.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                slotType + "[" + index + "] in " + target.getName().getString() + "'s accessories is empty"
            ), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                slotType + "[" + index + "] in " + target.getName().getString() + "'s accessories: " + 
                item.getCount() + "x " + item.getDisplayName().getString()
            ), false);
        }
        
        return 1;
    }
    
    private static int clearPlayerAccessories(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        
        AccessoriesHelper.clearAllAccessories(target);
        
        ctx.getSource().sendSuccess(() -> Component.literal(
            "Cleared all accessories from " + target.getName().getString()
        ), true);
        
        return 1;
    }
}
