# Quick Command Reference

## Inventory Editing

### Basic Inventory Operations
```bash
# Set item in player's slot 0
/editinventory player Steve set 0 minecraft:diamond_sword

# Get item from player's slot 0
/editinventory player Steve get 0

# List all items in player's inventory
/editinventory player Steve list

# Clear player's entire inventory
/editinventory player Steve clear
```

### Accessories Operations
```bash
# List all accessories a player has
/editinventory accessories Steve list

# Set a totem in the totem slot
/editinventory accessories Steve set totem 0 minecraft:totem_of_undying

# Get item from elytra slot
/editinventory accessories Steve get elytra 0

# Clear all accessories
/editinventory accessories Steve clear
```

## Backpack Management

### Listing and Viewing
```bash
# List all backpacks a player has
/backpack list Steve

# List all backups in the system
/backpack listbackups

# List backups for specific player
/backpack listbackups Steve
```

### Backup Operations
```bash
# Create a backup of backpack at index 0
/backpack backup Steve 0 my_backup_name

# Restore a backup to player's inventory
/backpack restore Steve UUID_my_backup_name_2024-01-15_10-30-45.dat

# Delete a backup
/backpack delete UUID_my_backup_name_2024-01-15_10-30-45.dat
```

### Backpack Editing
```bash
# Clear backpack at index 0
/backpack clear Steve 0
```

## Common Use Cases

### Transfer Items Between Players
```bash
# Step 1: Get item from source player
/editinventory player Alice get 5

# Step 2: Note the item and set it on target player
/editinventory player Bob set 5 minecraft:diamond_pickaxe
```

### Backup Before Risky Activity
```bash
# Step 1: List player's backpacks
/backpack list Steve

# Step 2: Create backup of important backpack
/backpack backup Steve 0 before_nether_trip

# Step 3: If needed, restore later
/backpack listbackups Steve
/backpack restore Steve <backup_filename>
```

### Give Totem to Player
```bash
# Give totem in accessories slot
/editinventory accessories Steve set totem 0 minecraft:totem_of_undying
```

### Emergency Inventory Clear
```bash
# Clear everything
/editinventory player Steve clear
/editinventory accessories Steve clear
```

## Tips

- All commands require OP level 2
- Works on both online and offline players
- Backups are timestamped automatically
- Use tab completion for player names and item IDs
- Slot numbers for main inventory: 0-40
- Accessory slot types depend on your configuration

## Slot Numbers

**Player Inventory:**
- 0-8: Hotbar
- 9-35: Main inventory
- 36-39: Armor slots
- 40: Offhand

**Accessories:**
- Depends on your configuration
- Use `/editinventory accessories <player> list` to see available slots
