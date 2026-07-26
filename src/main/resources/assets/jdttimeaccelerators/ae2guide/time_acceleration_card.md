---
navigation:
  parent: jdtta-index.md
  title: Time Acceleration Card
  icon: jdttimeaccelerators:ae2_time_acceleration_card
  position: 0
categories:
  - jdtta acceleration
item_ids:
  - jdttimeaccelerators:ae2_time_acceleration_card
---

# Time Acceleration Card

<Row>
  <ItemImage id="jdttimeaccelerators:ae2_time_acceleration_card" scale="4" />
  <RecipeFor id="jdttimeaccelerators:ae2_time_acceleration_card" />
</Row>

Install the card in a supported <ItemLink id="ae2:interface" /> or
<ItemLink id="ae2:pattern_provider" />. It spends power and Time Fluid from the
connected ME network to accelerate eligible adjacent blocks.

The speed button chooses the multiplier. Every accelerated target is funded
independently, so a provider surrounded by four machines can accelerate all four.
If the network cannot pay a target's complete cost, that target is skipped.

## Choosing Target Sides

Full-block Interfaces and Pattern Providers have a side-configuration button.
Each adjacent side can be selected independently. The screen displays the block
or cable part found on every side and marks selected sides that do not currently
contain an eligible target.

**Select All** enables all six sides and **Clear** disables all six.

> **Be careful with Select All.** Just Dire Things permits acceleration of
> ticking block entities and some randomly ticking blocks. An unrelated eligible
> machine or crop on a selected side can therefore consume power and Time Fluid.

Cable-part Interfaces and Pattern Providers do not show the side button. Their
only target is the block directly in front of the installed part.

## Pattern Provider Modes

- **Always** accelerates every eligible target on the selected sides.
- **Crafting Only** follows the exact adjacent machine that accepted the most
  recent pattern push. Its side must still be selected.

## Interface Modes

- **Always** continuously accelerates eligible targets on the selected sides.
- **Redstone Signal** accelerates them only while the Interface receives a
  redstone signal.

There is no common AE2, Minecraft, or NeoForge API that reliably reports whether
an arbitrary machine from another mod is actively working. Redstone control is
therefore used instead of unreliable automatic work detection.

