# GuSMP Resources Mod

A comprehensive Fabric mod for Minecraft 1.21+ that adds powerful inventory editing commands with full integration for Accessories and Traveller's Backpacks mods.

## Features

✅ **Inventory Editing**
- Edit online and offline player inventories
- Complete NBT-based persistence for offline players
- Proper client synchronization for online players

✅ **Accessories Integration**
- Full Accessories mod API integration
- Edit all accessory slot types (totem, elytra, custom slots)
- List and manage accessories for any player

✅ **Traveller's Backpacks Support**
- Detect and manage backpacks in inventory or accessories
- Create timestamped backups of backpacks
- Restore backpacks from backups
- Clear backpack contents

✅ **Advanced Features**
- Mixin-based integration for proper game compatibility
- Atomic file operations to prevent data corruption
- Comprehensive command suite (16 commands total)
- OP level 2 required for all commands

## Commands

### Inventory Commands (`/editinventory`)
- `/editinventory player <player> set/get/clear/list` - Manage player inventory
- `/editinventory accessories <player> set/get/clear/list` - Manage accessories

### Backpack Commands (`/backpack`)
- `/backpack list <player>` - List player's backpacks
- `/backpack backup <player> <index> <name>` - Create backup
- `/backpack restore <player> <backup>` - Restore from backup
- `/backpack listbackups [player]` - List all backups
- `/backpack delete <backup>` - Delete backup
- `/backpack clear <player> <index>` - Clear backpack

See [INVENTORY_COMMANDS.md](INVENTORY_COMMANDS.md) for complete documentation.

## Dependencies

- Minecraft 1.21+
- Fabric Loader 0.18.2+
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
2. Install required dependencies (Fabric API, Accessories, Traveller's Backpack)
3. Place this mod JAR in your `mods/` folder
4. Restart server

## Setup

For setup instructions please see the [fabric documentation page](https://docs.fabricmc.net/develop/getting-started/setting-up) that relates to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
