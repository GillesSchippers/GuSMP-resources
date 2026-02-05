# Technical Implementation Details

## Architecture Overview

This mod implements a comprehensive inventory editing system for Minecraft 1.21+ using the Fabric modding framework, with deep integration into the Accessories mod and Traveller's Backpacks mod.

## Core Components

### 1. Command System

**EditInventoryCommand**
- Implements Brigadier command tree
- Provides 8 subcommands for inventory/accessories manipulation
- Supports both online and offline players
- Uses ItemArgument for item specification

**BackpackBackupCommand**
- Implements 8 subcommands for backpack management
- Integrates with BackupManager for persistence
- Provides listing, backup, restore, and delete operations

### 2. Utility Layer

**InventoryManager**
- Handles both online and offline player inventories
- Loads/saves offline player data from NBT files
- Implements atomic file operations for safety
- Provides slot-based access to inventory

**AccessoriesHelper**
- Integrates with Accessories mod API
- Uses AccessoriesCapability for slot access
- Supports all slot types dynamically
- Handles offline player accessories via NBT

**BackpackHelper**
- Detects Traveller's Backpack items
- Searches inventory and accessories
- Provides NBT-based content access
- Supports backpack indexing across sources

**BackupManager**
- Creates timestamped backups
- Stores in world/backpacks_backup/ directory
- Implements backup listing and filtering
- Provides restore functionality

### 3. Mixin Integration

**ServerPlayerInventoryMixin**
```java
@Mixin(ServerPlayer.class)
```
- Enhances inventory synchronization
- Ensures changes are broadcast to client
- Provides syncInventory() helper method

**AccessoriesIntegrationMixin**
```java
@Mixin(value = ServerPlayer.class, priority = 1100)
```
- Hooks into player tick
- Ensures accessories capability sync
- Maintains compatibility with Accessories mod

**TravelersBackpackMixin**
```java
@Mixin(value = ItemStack.class, priority = 1100)
```
- Preserves backpack NBT data
- Hooks into tag modification
- Ensures backpack data integrity

## Data Flow

### Online Player Inventory Edit
1. Command executed → EditInventoryCommand
2. EntityArgument resolves to ServerPlayer
3. InventoryManager accesses live inventory
4. Modification applied
5. ServerPlayerInventoryMixin syncs to client

### Offline Player Inventory Edit
1. Command executed → EditInventoryCommand
2. InventoryManager loads playerdata NBT
3. Inventory created from NBT
4. Modification applied
5. NBT saved atomically to disk

### Backpack Backup
1. Command executed → BackpackBackupCommand
2. BackpackHelper finds backpack by index
3. BackupManager creates CompoundTag
4. Timestamp and metadata added
5. NBT written to backup directory

### Accessories Integration
1. AccessoriesHelper.get() called
2. AccessoriesCapability retrieved from player
3. Containers map accessed by slot type
4. AccessoriesContainer.getAccessories() provides inventory
5. Modification applied through container

## NBT Structure

### Offline Player Accessories
```nbt
accessories: {
  totem: [
    {Slot:0, id:"minecraft:totem_of_undying", Count:1}
  ],
  elytra: [
    {Slot:0, id:"minecraft:elytra", Count:1}
  ]
}
```

### Backpack Backup
```nbt
{
  player_uuid: "UUID-STRING",
  backpack_name: "backup_name",
  timestamp: 1234567890L,
  backpack: {
    // Full ItemStack NBT
  }
}
```

## Error Handling

### Command Errors
- Invalid slot numbers → User-friendly error message
- Missing players → "Player not found" message
- Invalid items → Brigadier syntax error
- IO errors → Exception message to executor

### File Operations
- Atomic writes using .tmp files
- Backup files created before overwrites
- NbtAccounter.unlimitedHeap() for large inventories
- IOException handling with user feedback

## Performance Considerations

### Optimization Strategies
1. **Lazy Loading**: Offline data loaded only when needed
2. **Caching**: AccessoriesCapability cached per operation
3. **Batch Operations**: Future enhancement for bulk edits
4. **Async IO**: Future enhancement for large operations

### Memory Usage
- Temporary Inventory objects for offline players
- NBT parsing on-demand
- No persistent caches (minimal memory footprint)

## Security

### Permission Levels
- All commands require OP level 2
- No client-side execution
- Server-side validation only

### Data Integrity
- Atomic file operations prevent corruption
- NBT validation before restoration
- Backup verification before restore

## Compatibility

### Mod Dependencies
- **Accessories**: Uses official API, compatible with future versions
- **Traveller's Backpacks**: NBT-based, resilient to API changes
- **Fabric API**: Uses stable command registration

### Minecraft Versions
- Designed for 1.21+
- Uses Mojang mappings
- Compatible with official Minecraft updates

## Extension Points

### Adding New Commands
1. Create command class in `commands/` package
2. Implement register(CommandDispatcher) method
3. Add to GustavDevMod.onInitialize()

### Adding New Utilities
1. Create class in `util/` package
2. Implement static helper methods
3. Document public API

### Adding New Mixins
1. Create mixin class in `mixin/` package
2. Add to gustavdev.mixins.json
3. Use appropriate priority level

## Testing Checklist

When building and testing this mod:

- [ ] Test online player inventory editing
- [ ] Test offline player inventory editing
- [ ] Test accessories listing and editing
- [ ] Test backpack detection
- [ ] Test backpack backup creation
- [ ] Test backpack restoration
- [ ] Test backup listing and deletion
- [ ] Test with multiple players
- [ ] Test with empty inventories
- [ ] Test with full inventories
- [ ] Test error cases (invalid slots, etc.)
- [ ] Verify file integrity after operations
- [ ] Check client synchronization
- [ ] Test with various item types
- [ ] Verify backup timestamps
- [ ] Test cross-mod compatibility

## Known Limitations

1. **Network Restrictions**: Cannot test build in restricted environments
2. **API Dependencies**: Relies on external mod APIs that may change
3. **NBT Structure**: Traveller's Backpack NBT structure is assumed
4. **Performance**: Large inventories may cause brief lag on offline load

## Future Enhancements

### Potential Features
- Bulk inventory operations
- Inventory templates
- Scheduled backups
- Backup rotation policy
- Web-based inventory viewer
- Inventory diff/compare tool
- Import/export functionality
- Permission-based restrictions

### Performance Improvements
- Async file I/O
- Inventory caching layer
- Batch operation API
- Lazy NBT parsing

## Development Setup

### Prerequisites
- JDK 21
- Gradle (via wrapper)
- Internet access for dependencies

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew runServer
```

### IDE Setup
```bash
./gradlew idea  # For IntelliJ IDEA
./gradlew eclipse  # For Eclipse
```

## Contributing

When contributing to this mod:
1. Follow existing code style
2. Add JavaDoc for public methods
3. Include error handling
4. Test thoroughly
5. Update documentation

## License

CC0 - Public Domain
