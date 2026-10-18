# Grafter Client (Minecraft 26.2)

Client-side Fabric mod for Minecraft 26.2 with a black & neon green UI. Press **Right Shift** in game to open the menu. Left click a module to toggle it, right click to open its settings.

Categories: **Combat** (Anchor Macro, Auto Crystal, Auto Hit Crystal, Auto Inv Totem, Auto Totem, Double Anchor, Hover Totem, Elytra Swap, HitBox, Mace Swap, Static HitBoxes, Aim Assist, Auto Clicker, Criticals, Velocity, Kill Aura, TriggerBot, Fakelag), **Render** (Player/Chest/Spawner/Block ESP with tracers, Freecam, SUS Chunks, Anti Vanish, SlowSwing, Tool Angle, HUD), **Misc** (Auto Eat, Auto Firework, Auto Log, Auto Loot, Auto Mine, Auto Tool, Auto TPA, Cord Snapper, Elytra Glide, Schematic Builder, Inventory Move) and **Donut** (Anti Trap, Auction Sniper, Order Filler, Auto Sell, Auto Spawner Sell, Item Dropper, RTP Base Finder).

Text settings are typed in chat: `.help`, `.set <key> <value>`, `.show`.

## Build
- GitHub: push to `main`, then Actions tab > latest run > download the `Grafters-26.2-jar` artifact.
- Local (JDK 25 + Gradle 9.5.1): `gradle build` -> `build/libs/grafters-1.0.0.jar`

Requires Fabric Loader 0.19.3+ and Fabric API for 26.2. Bump versions in `gradle.properties` (see https://fabricmc.net/develop).


Schematic Builder needs Litematica (and malilib) installed; Grafters works without them.
