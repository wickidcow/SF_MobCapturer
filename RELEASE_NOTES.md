# SF MobCapturer 1.0.6

Boot-compatibility fix for the Slimefun Legacy edition of MobCapturer.

## Highlights

- Fixes the Paper 26.2 startup failure caused by an incompatible bStats `Metrics` constructor being linked at runtime.
- Removes MobCapturer's optional bStats startup dependency so another plugin's or library's bundled bStats classes cannot prevent the addon from enabling.
- Keeps the universal release baseline at **Minecraft 1.21.11 / Java 21 bytecode** while continuing compile validation against **Paper 26.2**, **Purpur 26.2**, **Paper 26.3**, and **Folia 26.2**.
- Uses **Slimefun Legacy 4.1.50** as the CI API target.
- Keeps Purpur 26.3 validation availability-aware until a matching Purpur API artifact is published.
- Adds release-artifact checks that fail CI if bStats classes or references are accidentally reintroduced.
- Retains the EliteMobs/MythicMobs capture protections, modern mob support, Folia metadata, and English compatibility fixes from earlier releases.
- Release asset is provided directly as `SF_MobCapturer1.0.6.jar`.

## Upgrade

Stop the server, remove `SF_MobCapturer1.0.5.jar`, install `SF_MobCapturer1.0.6.jar`, and start the server normally. Do not use `/reload`.

Existing captured mobs and MobCapturer configuration remain compatible; this release changes startup packaging only and does not alter capture data formats.
