# Inventory Editing Commands - GuSMP Resources Mod

## Overview
This mod adds comprehensive inventory editing commands for online and offline players, with full integration for Accessories mod and Traveller's Backpacks mod. All features use mixins for proper game integration.

## Features
- ✅ Edit online and offline player inventories
- ✅ Edit Accessories mod slots
- ✅ Edit Traveller's Backpacks (from inventory or accessories)
- ✅ Create backups of backpacks
- ✅ Restore backpacks from backups
- ✅ All operations use mixins for proper integration
- ✅ NBT-based persistence for offline players

## Commands

### Inventory Editing Commands (`/editinventory`)

All commands require OP level 2 or higher.

#### Player Inventory Commands

**Set item in inventory slot:**
```
/editinventory player <player> set <slot> <item>
```
- `<player>` - Target player (online or offline)
- `<slot>` - Slot number (0-40 for main inventory)
- `<item>` - Item to place in slot
- Example: `/editinventory player Steve set 0 minecraft:diamond_sword`

**Get item from inventory slot:**
```
/editinventory player <player> get <slot>
```
- Shows what item is in the specified slot
- Example: `/editinventory player Steve get 0`

**Clear entire inventory:**
```
/editinventory player <player> clear
```
- Removes all items from player's inventory
- Example: `/editinventory player Steve clear`

**List all inventory items:**
```
/editinventory player <player> list
```
- Shows all non-empty slots in player's inventory
- Example: `/editinventory player Steve list`

#### Accessories Commands

**List all accessories:**
```
/editinventory accessories <player> list
```
- Shows all equipped accessories by slot type
- Example: `/editinventory accessories Steve list`

**Set accessory in slot:**
```
/editinventory accessories <player> set <slotType> <index> <item>
```
- `<slotType>` - Type of accessory slot (e.g., "totem", "elytra")
- `<index>` - Index within that slot type
- `<item>` - Item to place in slot
- Example: `/editinventory accessories Steve set totem 0 minecraft:totem_of_undying`

**Get accessory from slot:**
```
/editinventory accessories <player> get <slotType> <index>
```
- Shows what item is in the specified accessory slot
- Example: `/editinventory accessories Steve get totem 0`

**Clear all accessories:**
```
/editinventory accessories <player> clear
```
- Removes all accessories from player
- Example: `/editinventory accessories Steve clear`

### Backpack Commands (`/backpack`)

All commands require OP level 2 or higher.

#### List Backpacks

**List player's backpacks:**
```
/backpack list <player>
```
- Shows all backpacks in player's inventory and accessories
- Displays index numbers for use with other commands
- Example: `/backpack list Steve`

#### Backup Management

**Create a backup:**
```
/backpack backup <player> <backpackIndex> <name>
```
- `<backpackIndex>` - Index from `/backpack list` command
- `<name>` - Name for this backup
- Saves to `world/backpacks_backup/` directory
- Example: `/backpack backup Steve 0 important_items`

**Restore from backup:**
```
/backpack restore <player> <backupFileName>
```
- `<backupFileName>` - Name of backup file to restore
- Adds restored backpack to player's inventory
- Example: `/backpack restore Steve UUID_important_items_2024-01-15_10-30-45.dat`

**List all backups:**
```
/backpack listbackups
```
- Shows all backpack backups in the system
- Displays filename, player UUID, backup name, and timestamp

**List player's backups:**
```
/backpack listbackups <player>
```
- Shows only backups for specific player
- Example: `/backpack listbackups Steve`

**Delete a backup:**
```
/backpack delete <backupFileName>
```
- Permanently deletes a backup file
- Example: `/backpack delete UUID_important_items_2024-01-15_10-30-45.dat`

#### Backpack Editing

**Clear backpack contents:**
```
/backpack clear <player> <backpackIndex>
```
- Removes all items from specified backpack
- Example: `/backpack clear Steve 0`

## Implementation Details

### Offline Player Support
The mod can edit inventories of offline players by directly reading and writing their playerdata NBT files. Changes are persisted and will be available when the player logs in.

### Accessories Integration
Full integration with the Accessories mod using the Accessories API:
- Access to all accessory slot types
- Support for custom accessory slots
- Proper synchronization with client

### Traveller's Backpacks Integration
Integration with Traveller's Backpacks mod:
- Detect backpacks in inventory and accessories
- Access backpack contents via NBT
- Create timestamped backups
- Restore backpacks with full contents

### Mixins
Three mixins provide deep integration:
1. **ServerPlayerInventoryMixin** - Ensures inventory changes sync properly
2. **AccessoriesIntegrationMixin** - Hooks into Accessories mod for proper slot handling
3. **TravelersBackpackMixin** - Preserves backpack NBT data when modified

### Backup System
Backups are stored in the world's `backpacks_backup/` directory with the following naming:
```
<playerUUID>_<backupName>_<timestamp>.dat
```

Each backup contains:
- Full backpack NBT data
- Player UUID
- Backup name
- Timestamp

## Dependencies
- Fabric Loader 0.18.2+
- Minecraft 1.21+
- Fabric API
- Accessories mod 1.2.4+
- Traveller's Backpack mod 10.2.4+

## Building
```bash
./gradlew build
```

The mod JAR will be in `build/libs/`.

## Installation
1. Install Fabric Loader for Minecraft 1.21+
2. Install Fabric API
3. Install Accessories mod
4. Install Traveller's Backpack mod
5. Place this mod JAR in your `mods/` folder

## Support
This mod is designed for server operators and requires OP level 2 to use any commands.

## Safety
- All operations create proper backups when using the backup system
- Offline player data is saved atomically to prevent corruption
- Online player inventories are properly synced to clients
- Error messages guide proper usage

## Example Workflows

### Moving items between players
```
# Get item from one player
/editinventory player Alice get 0

# Set it on another player
/editinventory player Bob set 0 minecraft:diamond_sword
```

### Backing up and restoring a backpack
```
# List player's backpacks
/backpack list Steve

# Create backup
/backpack backup Steve 0 before_adventure

# Later, restore it
/backpack listbackups Steve
/backpack restore Steve <filename>
```

### Managing accessories
```
# See what accessories a player has
/editinventory accessories Steve list

# Give a totem
/editinventory accessories Steve set totem 0 minecraft:totem_of_undying
```
