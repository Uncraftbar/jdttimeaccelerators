# JDT Time Accelerators

JDT Time Accelerators is a NeoForge addon focused on automating the Time Wand from [Just Dire Things](https://www.curseforge.com/minecraft/mc-mods/just-dire-things). Use standalone powered machines for single-block or area acceleration, or install the Time Acceleration Card in Applied Energistics 2 Interfaces and Pattern Providers to power acceleration directly from your ME network.

The goal is to preserve the Time Wand's familiar multipliers, costs, and configuration while making time acceleration practical in unattended automation.

## Features

### Simple Time Accelerator

- Accelerates the single block directly in front of the machine.
- Uses Forge Energy and, when configured by Just Dire Things, Time Fluid.
- Supports redstone control.
- Offers Time Wand-style speed selection up to one quarter of the configured maximum multiplier.

### Advanced Time Accelerator

- Accelerates blocks in a configurable area using the familiar Just Dire Things area controls.
- Uses Forge Energy and Time Fluid.
- Supports redstone control.
- Can use the full configured Time Wand multiplier range.

### Applied Energistics 2 integration

The optional **Time Acceleration Card** turns supported ME Interfaces and Pattern Providers into network-powered time accelerators.

- Draws AE power and Time Fluid from the connected ME network.
- Supports independent selection of all six adjacent sides on full-block hosts.
- Keeps cable-part hosts fixed to the block directly in front of the part.
- Charges every eligible selected target independently.
- Gives Pattern Providers **Always** and **Crafting Only** modes.
- Gives Interfaces **Always** and **Redstone Signal** modes.
- Includes an AE2 Guide page with setup and safety information.

Compatibility is included for standard AE2 devices and provider/interface variants from ExpandedAE, ExtendedAE, MEGA Cells, and AdvancedAE.

## Server configuration

The world-specific `serverconfig/jdttimeaccelerators-server.toml` file provides two shared limits for standalone machines and AE2 cards:

- `maxMultiplier = 0` follows the maximum configured for the Just Dire Things Time Wand. A positive value applies a lower cap, rounded down to a supported power-of-two multiplier.
- `maxStacksPerTarget = 1` prevents several accelerators from stacking on the same block during one server tick. Pack authors can raise it to allow a controlled number of machines and cards to stack.

Every successful stack pays its own full energy and Time Fluid cost. Failed and over-limit attempts consume nothing.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.209` or newer in the `1.21.1` line
- Just Dire Things `1.5.7` or newer
- Applied Energistics 2 `19.2.0` or newer is optional and only required for the Time Acceleration Card

## Recipes and usage

Recipes are available through JEI and other recipe viewers.

For the standalone machines:

1. Place a Simple or Advanced Time Accelerator.
2. Select its target or configure the Advanced tier's area.
3. Supply Forge Energy and any required Time Fluid.
4. Configure redstone behavior and the desired multiplier.

For AE2 integration:

1. Store Time Fluid and sufficient power in the ME network.
2. Insert a Time Acceleration Card into a supported Interface or Pattern Provider.
3. Choose the acceleration multiplier and operating mode.
4. On full blocks, open the target-side screen and select every adjacent machine that should be accelerated.

The displayed multiplier is the actual Time Wand-style acceleration rate; there is no hidden machine-speed multiplier layered on top.

## Building from source

```bash
./gradlew build
```

The built JAR is written to `build/libs/`.

## License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).

Some block textures are derived from Just Dire Things textures, which are MIT licensed. See `src/main/resources/META-INF/licenses/JDT_TEXTURE_LICENSE_NOTICE.txt` for the upstream notice.

The AE2 Time Acceleration Card texture is adapted from Applied Energistics 2 and is licensed separately under CC BY-NC-SA 3.0. See `src/main/resources/META-INF/licenses/AE2_TEXTURE_LICENSE_NOTICE.txt` for attribution and license details.
