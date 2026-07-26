# JDT Time Accelerators 2.1.0 — Controlled Acceleration

Version 2.1.0 makes acceleration predictable when several standalone machines or AE2 card hosts target the same block, and exposes the new controls through NeoForge's in-game config screen.

## Added

- Added a shared maximum acceleration multiplier setting for standalone Time Accelerators and AE2 Time Acceleration Cards.
- Added a configurable per-target stack limit. The default of one prevents multiple accelerators from multiplying the speed of the same block during one server tick.
- Added an in-game configuration screen under **Mods → JDT Time Accelerators → Config** while playing in a local world.
- Added translated names and descriptions for the acceleration settings.

## Fixed

- Prevented standalone machines and AE2 card hosts from bypassing the shared stack limit when targeting the same block.
- Prevented rejected or failed acceleration attempts from consuming Forge Energy, AE power, or Time Fluid.
- Corrected the documented config path to `config/jdttimeaccelerators-server.toml`.

This release targets Minecraft 1.21.1 on NeoForge.
