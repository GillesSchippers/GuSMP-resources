# Chunky Auto-Pregeneration Feature

## Overview
This feature automatically manages world pregeneration using the Chunky mod. It intelligently starts and stops pregeneration tasks based on player activity to minimize server lag.

## Requirements
- Chunky mod must be installed on the server
- Chunky pregeneration tasks must be configured (see Chunky documentation)

## How It Works

### Automatic Task Management
1. **Server Startup**: After a 60-second delay (to prevent startup lag), the manager checks if players are online
2. **No Players Online**: Automatically resumes/continues any configured Chunky pregeneration tasks
3. **Player Joins**: Immediately pauses all pregeneration tasks to ensure smooth gameplay
4. **Player Leaves**: When the last player disconnects, pregeneration automatically resumes

### Startup Delay
The 60-second startup delay prevents the pregeneration from starting immediately when the server boots, which could cause lag during the critical startup phase.

## Configuration

### Chunky Task Setup
Before this feature can work, you need to set up Chunky pregeneration tasks. Use Chunky's commands to configure tasks for your worlds:

```
/chunky world <world-name>
/chunky center <x> <z>
/chunky radius <blocks>
/chunky start
```

The GuSMP auto-pregeneration feature will then automatically pause and resume these tasks based on player activity.

### Adjusting Startup Delay
If you want to change the startup delay (default: 60 seconds), modify the `STARTUP_DELAY_SECONDS` constant in `ChunkyPregenManager.java`.

## Logging
The feature logs all its activities to help you monitor its behavior:
- Manager initialization
- Chunky API connection status
- Task pause/resume events
- Player join/leave triggers
- Startup delay completion

Check your server logs for messages prefixed with the mod's logger.

## Troubleshooting

### Pregeneration doesn't start
- Verify Chunky mod is installed
- Check that you have configured at least one Chunky pregeneration task
- Ensure the task is not completed
- Check server logs for error messages

### Pregeneration doesn't pause when players join
- Check server logs to verify the event is being triggered
- Ensure Chunky API is properly connected (check startup logs)

### Server lags on startup
- The 60-second startup delay should prevent this, but if lag persists, consider:
  - Increasing the startup delay
  - Reducing Chunky's task chunk batch size
  - Allocating more RAM to the server

## Technical Details
- Uses Fabric's `ServerLifecycleEvents` for server start/stop detection
- Uses Fabric's `ServerPlayConnectionEvents` for player join/disconnect detection
- Integrates with Chunky API via `ChunkyAPIProvider`
- Thread-safe implementation with daemon thread for startup delay
