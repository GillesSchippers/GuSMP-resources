# Project Structure

## Complete File Tree

```
GuSMP-resources/
├── Documentation (5 files)
│   ├── README.md                        - Project overview and features
│   ├── INVENTORY_COMMANDS.md            - Complete command reference (245 lines)
│   ├── QUICK_REFERENCE.md               - Quick command guide (123 lines)
│   ├── TECHNICAL.md                     - Developer documentation (278 lines)
│   └── IMPLEMENTATION_SUMMARY.md        - Implementation summary (193 lines)
│
├── Build Configuration (4 files)
│   ├── build.gradle                     - Dependencies and build config
│   ├── gradle.properties                - Version properties
│   ├── settings.gradle                  - Gradle settings
│   └── gradlew / gradlew.bat            - Gradle wrapper scripts
│
└── Source Code (src/main/)
    ├── java/com/gustavserv/gustavdev/
    │   ├── GustavDevMod.java            - Main mod initializer (26 lines)
    │   │
    │   ├── commands/
    │   │   ├── EditInventoryCommand.java      - Inventory commands (252 lines)
    │   │   └── BackpackBackupCommand.java     - Backpack commands (276 lines)
    │   │
    │   ├── util/
    │   │   ├── InventoryManager.java          - Inventory ops (115 lines)
    │   │   ├── AccessoriesHelper.java         - Accessories API (167 lines)
    │   │   ├── BackpackHelper.java            - Backpack ops (173 lines)
    │   │   └── BackupManager.java             - Backup system (193 lines)
    │   │
    │   └── mixin/
    │       ├── ServerPlayerInventoryMixin.java  - Sync mixin (25 lines)
    │       ├── AccessoriesIntegrationMixin.java - Accessories (33 lines)
    │       └── TravelersBackpackMixin.java      - Backpack (32 lines)
    │
    └── resources/
        ├── fabric.mod.json              - Mod metadata
        ├── gustavdev.mixins.json        - Mixin configuration
        │
        ├── assets/gustavdev/
        │   ├── lang/en_us.json
        │   └── textures/               - (existing resources)
        │
        └── data/
            ├── gustavdev/
            │   ├── accessories/        - Accessories config
            │   └── puffish_skills/     - (existing data)
            └── farmersdelight/         - (existing data)
```

## Code Statistics

### Total Lines of Code: 2,048

**Java Code: 1,292 lines**
- Main: 26
- Commands: 528 (252 + 276)
- Utilities: 648 (115 + 167 + 173 + 193)
- Mixins: 90 (25 + 33 + 32)

**Documentation: 713 lines**
- INVENTORY_COMMANDS.md: 245
- TECHNICAL.md: 278
- QUICK_REFERENCE.md: 123
- README.md updates: 67

**Configuration: 43 lines**
- Various config file updates

## Package Structure

```
com.gustavserv.gustavdev
│
├── GustavDevMod                 (Main Initializer)
│
├── commands/                    (Command Implementations)
│   ├── EditInventoryCommand     - 8 inventory/accessories commands
│   └── BackpackBackupCommand    - 8 backpack management commands
│
├── util/                        (Utility Classes)
│   ├── InventoryManager         - Online/offline inventory operations
│   ├── AccessoriesHelper        - Accessories mod integration
│   ├── BackpackHelper           - Backpack detection and operations
│   └── BackupManager            - Backup creation and restoration
│
└── mixin/                       (Mixin Classes)
    ├── ServerPlayerInventoryMixin    - Inventory synchronization
    ├── AccessoriesIntegrationMixin   - Accessories mod hooks
    └── TravelersBackpackMixin        - Backpack mod hooks
```

## Data Flow Diagram

```
Command Execution Flow:
┌─────────────────────────────────────────────────────────┐
│ Player executes /editinventory or /backpack command    │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ Brigadier Command System                                │
│ ├── EditInventoryCommand.register()                    │
│ └── BackpackBackupCommand.register()                   │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌──────────────────┐
│ Utility Layer   │    │ Mixin Layer      │
│ ├── Inventory   │    │ ├── Inventory    │
│ ├── Accessories │    │ ├── Accessories  │
│ ├── Backpack    │    │ └── Backpack     │
│ └── Backup      │    └──────────────────┘
└────────┬────────┘             │
         │                      │
         │     ┌────────────────┘
         │     │
         ▼     ▼
┌─────────────────────────────────────────────────────────┐
│ Data Layer                                              │
│ ├── Online Player → ServerPlayer.getInventory()        │
│ ├── Offline Player → NBT Files (playerdata/)           │
│ ├── Accessories → AccessoriesCapability API            │
│ └── Backups → NBT Files (backpacks_backup/)            │
└─────────────────────────────────────────────────────────┘
```

## Command Tree

```
/editinventory
├── player <target>
│   ├── set <slot> <item>
│   ├── get <slot>
│   ├── clear
│   └── list
└── accessories <target>
    ├── list
    ├── set <slotType> <index> <item>
    ├── get <slotType> <index>
    └── clear

/backpack
├── list <target>
├── backup <target> <index> <name>
├── restore <target> <backupFile>
├── listbackups
├── listbackups <target>
├── delete <backupFile>
└── clear <target> <index>
```

## Integration Points

```
GuSMP Resources Mod
        │
        ├─── Fabric API
        │     └─── Command Registration
        │
        ├─── Accessories Mod
        │     ├─── AccessoriesCapability
        │     ├─── AccessoriesContainer
        │     └─── Slot Management
        │
        └─── Traveller's Backpacks
              ├─── ItemStack Detection
              ├─── NBT Data Access
              └─── Backup System
```

## File Size Breakdown

```
Category              Files  Lines   Percentage
─────────────────────────────────────────────────
Java Code              10    1,292   63.1%
Documentation           4      713   34.8%
Configuration           4       43    2.1%
─────────────────────────────────────────────────
Total                  18    2,048   100%
```

## Mixin Priority Levels

```
ServerPlayerInventoryMixin     - Default Priority (1000)
AccessoriesIntegrationMixin    - Priority 1100
TravelersBackpackMixin         - Priority 1100
```

## Build Dependencies

```
Dependencies Tree:
├── Minecraft 1.21.11
├── Fabric Loader 0.18.2+
├── Fabric API 0.139.4+1.21.11
├── Accessories 1.2.4+1.21
└── Traveller's Backpack 10.2.4+1.21
```
