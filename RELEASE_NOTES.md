# SF MobCapturer 1.0.0

First Slimefun Legacy release of MobCapturer, based on the newer 1.21.11-capable codebase.

## Highlights

- Built for Slimefun Legacy 4.1.37 and Paper 26.2.
- Builds with Java 25 while retaining Java 21-compatible bytecode.
- Uses stable namespaced attribute keys for newly captured mobs.
- Restores compatibility with older MobCapturer attribute names.
- Recovers affected Dev-37 mob eggs containing Paper `CraftAttribute{...}` data, including attributes such as `minecraft:fall_damage_multiplier`.
- Keeps the newer 1.21.11 mob support and horse equipment fixes from the maintained codebase.
- Release asset is provided directly as `SF_MobCapturer1.0.0.jar`.

## Upgrade

Replace the previous MobCapturer JAR with `SF_MobCapturer1.0.0.jar` and restart the server. Existing captured mob eggs should continue to work; eggs affected by the Paper attribute serialization issue are normalized when restored.
