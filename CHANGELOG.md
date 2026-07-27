# Changelog

## 2.2.0

### Changed

- Changed Pattern Provider Crafting Only mode to accelerate every selected side
  after a successful pattern push, regardless of which side accepted it.
- Added an optional Accepted Ingredients Only target mode that preserves the
  previous output-following behavior.
- Updated the in-game AE2 guide to explain Pattern Provider targeting modes.

### Fixed

- Fixed target-side buttons retaining their hovered appearance after being
  clicked.
- Fixed the targeting-mode button overlapping the side-selection controls.

## 2.1.0

### Added

- Added server configuration for a global acceleration multiplier cap and the number of allowed stacks per target.
- Added an in-game NeoForge configuration screen with translated setting names.

### Fixed

- Prevented standalone machines and AE2 card hosts from bypassing the stack limit when they target the same block in one server tick.
- Prevented rejected or failed acceleration attempts from consuming energy or Time Fluid.
- Fixed the documented server config location.

## 2.0.0

### Added

- Added the AE2 Time Acceleration Card for Interfaces and Pattern Providers.
- Added multi-side targeting, network-funded acceleration, addon compatibility, and AE2 Guide documentation.

### Changed

- Repositioned the project as general automation for the Just Dire Things Time Wand.
- Replaced unreliable Interface work detection with deterministic redstone control.
- Updated AE2 integration to use official upgrade associations and efficient grid ticking.

### Fixed

- Fixed card-slot acceptance and GUI behavior across supported AE2 addon devices.

## 0.1.0

Initial release.

### Added

- Added the Simple Time Accelerator.
- Added the Advanced Time Accelerator.
- Added Time Wand-style multiplier controls to the machine GUI.
- Added Forge Energy consumption based on the selected multiplier.
- Added Time Fluid consumption when configured by Just Dire Things.
- Added redstone control support through Just Dire Things machine behavior.
- Added configurable area acceleration for the Advanced tier.
- Added crafting recipes, loot tables, block/item models, textures, localization, and a creative tab.
