# JDT Time Accelerators

JDT Time Accelerators is a NeoForge addon for [Just Dire Things](https://www.curseforge.com/minecraft/mc-mods/just-dire-things) that turns Time Wand-style acceleration into placeable, powered machines.

The goal is simple: keep the predictable feel and costs of the Just Dire Things Time Wand, but make it usable in automation setups without requiring a player to stand there and click. Like civilization, but with fewer meetings.

## Features

- **Simple Time Accelerator**
  - Accelerates the single block directly in front of the machine.
  - Uses Forge Energy.
  - Uses Time Fluid when Just Dire Things configures a Time Wand fluid cost.
  - Speed is controlled from the machine GUI.
  - Maximum speed is capped to one quarter of the configured Just Dire Things Time Wand maximum multiplier.

- **Advanced Time Accelerator**
  - Accelerates blocks in a configurable area using the familiar Just Dire Things area controls.
  - Uses Forge Energy.
  - Uses Time Fluid when applicable.
  - Speed is controlled from the machine GUI.
  - Can use the full configured Just Dire Things Time Wand maximum multiplier.

- Both machines support the inherited Just Dire Things redstone behavior.
- Both machines are registered in their own creative tab.
- No Time Wand item is stored inside the machines.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.209` or newer in the `1.21.1` line
- Just Dire Things `1.5.7` or newer

## Recipes

### Simple Time Accelerator

Crafted from:

- Just Dire Things Ferricore Ingots
- 1 Clock
- 2 Lapis Lazuli
- 1 Redstone
- 1 Just Dire Things Time Crystal

### Advanced Time Accelerator

Crafted from:

- Just Dire Things Celestigem
- 2 Clocks
- 1 Redstone
- 1 Simple Time Accelerator

Exact layouts are available through JEI/recipe viewers in-game.

## Usage

1. Place a Time Accelerator.
2. Point the Simple tier at the block you want to accelerate, or configure the Advanced tier's area.
3. Supply Forge Energy.
4. Supply Time Fluid if your Just Dire Things configuration requires it for Time Wand acceleration.
5. Set the machine's redstone mode.
6. Use the **Wand Speed** controls in the GUI to select the multiplier.

The machines intentionally use Time Wand-style multipliers instead of the normal Just Dire Things machine tick-speed control, so the displayed multiplier is the behavior you get.

## Building from source

```bash
./gradlew build
```

The built jar is written to `build/libs/`.

For local development, the Gradle client run uses the username `Uncraftbar`.

## License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).

Some block textures are derived from Just Dire Things textures, which are MIT licensed. See `src/main/resources/assets/jdttimeaccelerators/textures/block/JDT_TEXTURE_LICENSE_NOTICE.txt` for the upstream notice.
