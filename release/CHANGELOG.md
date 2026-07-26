# JDT Time Accelerators 2.0.0 — AE2-Powered Time Acceleration

Version 2.0.0 expands JDT Time Accelerators from standalone machines into general automation for the Just Dire Things Time Wand.

## Added

- Added the Time Acceleration Card for Applied Energistics 2 Interfaces and Pattern Providers.
- Added direct consumption of AE power and Time Fluid from the connected ME network.
- Added independently selectable target sides for full-block Interfaces and Pattern Providers, including Select All, Clear, adjacent-block icons, and invalid-target warnings.
- Added support for accelerating multiple adjacent machines from one host.
- Added **Always** and **Crafting Only** modes for Pattern Providers.
- Added **Always** and **Redstone Signal** modes for Interfaces.
- Added compatibility for standard AE2 devices and variants from ExpandedAE, ExtendedAE, MEGA Cells, and AdvancedAE.
- Added a dedicated JDT Time Accelerators section inside the AE2 Guide.

## Improved

- Cable-part Interfaces and Pattern Providers now remain correctly locked to their installed face.
- Time Fluid and energy costs are simulated and charged independently for every accelerated target.
- Pattern Providers in Crafting Only mode follow the exact adjacent machine that accepted the crafting push.
- AE2 grid tick scheduling now sleeps when acceleration cannot be active and wakes when required.
- Upgrade-card compatibility and generated lore use AE2's official upgrade association API.
- Renamed the card to **Time Acceleration Card** to match AE2's card naming conventions.

## Fixed

- Fixed controls appearing when the card was only present in the player's inventory.
- Fixed addon provider/interface screens showing controls on cable parts.
- Fixed card acceptance and behavior across supported ExpandedAE, ExtendedAE, MEGA Cells, and AdvancedAE devices.
- Fixed duplicate AdvancedAE menu initialization and compatibility issues with its normal, small, and extended provider variants.

This release currently targets Minecraft 1.21.1. The NeoForge 26.1.2 port will follow separately.
