# v1.2.0 - Correct Time Wand Behavior and NeoForge 26.1.2

## Changes

- Matched Time Fluid consumption to Just Dire Things' cumulative Time Wand cost and amortized it across the effect duration.
- Scaled Time Fluid usage by the number of blocks successfully accelerated.
- Restored redstone signal detection and redstone mode behavior.
- Added full NeoForge 26.1.2 support, including corrected registration, item definitions, recipes, fluid initialization, and GUI text rendering.
- Prevented recursive acceleration by adding both accelerators to Just Dire Things' tick-speed deny tag.
