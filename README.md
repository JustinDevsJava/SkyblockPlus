# Skyblock+

Skyblock+ is a Fabric client-side utility mod for Hypixel SkyBlock. It focuses on quality-of-life tools, dungeon and boss helpers, render utilities, and a clean in-game configuration menu.

> This mod is client-side only. Some features may be considered risky on public servers. Use your own judgment and follow server rules.

## Supported Version

- Minecraft `1.21.11`
- Fabric Loader `0.18.0+`
- Fabric API `0.137.0+`

## Features

### General

- Modern ClickGUI opened with `Right Shift` by default
- Searchable feature menu
- Category sidebar
- Collapsible/scrollable option panels
- Configs save immediately when changed
- Optional top-right active feature HUD

### Automation

- Harp Bot
- Auto Tip
- Auto Experimentation
  - Chronomatron support
  - Ultrasequencer support

### Movement

- Auto Sprint

### Render

- Player Size
  - X/Y/Z sliders
- Etherwarp Preview
  - Optional failed target display
- Hide Players
  - Dungeon-only option
  - Hide-all option
  - Distance slider
- Wardrobe Keybinds
  - Warning shown in-menu because this may be bannable
  - Configurable keybinds for page navigation, unequip, and wardrobe slots 1-9

### HUD / Notifications

- Day Viewer
- RNG Drop Summary
- Entrance Notifier
- Feature List HUD toggle

### Gameplay

- Foraging Style Warning
- Legacy Ghost Pickaxe support
- Force Toggle Use on selected items
- Prevent Attacking on Goons

### Dungeons / Boss

Menu toggles are available for:

- Extra Stats
- Leap Menu
- Map Info
- Puzzle Solvers
- Terminal Simulator
- Terminal Solver
- Terminal Sounds
- Terminal Times
- Terminal Titles

### Patches

- 0 Ping Dungeonbreaker
- Always Use Spectator Fog
- Remove Suffocation Screen
- Cancel Shortbow Pull Animation
- Fix Dungeon Block Place
- No Command Execution Confirmation
- Overrule Skyblocker Glowing Depth Test

## Commands

Main command:

```text
/skyblock_plus
```

Useful subcommands:

```text
/skyblock_plus configSave
/skyblock_plus whereAmI
/skyblock_plus tps
/skyblock_plus resetAutoTip
/skyblock_plus resetLifeTimer
```

## Building

Requirements:

- Java 21+
- Gradle wrapper included

Build:

```powershell
.\gradlew.bat build
```

Output jar:

```text
build/libs/skyblock_plus-<version>+<minecraft_version>.jar
```

## Installation

1. Install Fabric Loader for Minecraft `1.21.11`.
2. Install Fabric API.
3. Put the Skyblock+ jar in your `mods` folder.
4. Launch Minecraft.
5. Press `Right Shift` in-game to open the Skyblock+ menu.
