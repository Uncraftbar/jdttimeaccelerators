# JDT Time Accelerators 2.2.0 - Flexible Crafting Targets

Version 2.2.0 makes Pattern Provider Crafting Only mode practical for input
hatches, multiblocks, subnet routing, and other setups where the block accepting
ingredients is not the block that should be accelerated.

## Changed

- Crafting Only now uses a successful pattern push as the trigger and accelerates
  every selected adjacent target by default.
- Added an **Accepted Input Only** targeting mode for setups that should follow
  the exact adjacent block that accepted the pattern.
- Updated the in-game AE2 guide with the new targeting behavior and controls.

## Fixed

- Fixed target-side buttons retaining their hovered appearance after being
  clicked.
- Reworked the target-selection layout so the targeting-mode button no longer
  overlaps the side controls.
- Preserved Pattern Provider GUI compatibility with supported AE2 addons.

This release targets Minecraft 1.21.1 on NeoForge.
