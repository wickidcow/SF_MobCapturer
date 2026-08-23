<div align="center">

# 👻 SF MobCapturer
### Mob capture tools for Slimefun Legacy

SF MobCapturer adds reusable mob-capture gameplay to Slimefun while preserving important entity data and protecting plugin-managed boss mobs from being converted into capture eggs.

[![Build](https://github.com/wickidcow/SF_MobCapturer/actions/workflows/ci.yml/badge.svg)](https://github.com/wickidcow/SF_MobCapturer/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/wickidcow/SF_MobCapturer?label=license)](LICENSE)
[![Java](https://img.shields.io/badge/Runtime-Java%2025-orange)](https://adoptium.net/)
[![Paper](https://img.shields.io/badge/Server-Paper%2026.2-blue)](https://papermc.io/)

[Download](https://github.com/wickidcow/SF_MobCapturer/releases) ·
[Builds](https://github.com/wickidcow/SF_MobCapturer/actions) ·
[Report a Bug](https://github.com/wickidcow/SF_MobCapturer/issues)

</div>

> [!IMPORTANT]
> **SF MobCapturer is an unofficial community-maintained fork of MobCapturer for Slimefun Legacy.**
> It preserves the original MobCapturer concept while maintaining compatibility with modern Paper servers and the Slimefun Legacy ecosystem.
>
> **NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.**

---

## ✨ What is SF MobCapturer?

SF MobCapturer adds a **Mob Cannon**, **Mob Capturing Pellets**, and reusable captured-mob eggs to Slimefun.

Aim the Mob Cannon at a supported creature and fire a capturing pellet. Successful captures preserve supported entity state such as health, potion effects, fire ticks, custom names, attributes, equipment, and mob-specific properties.

The project is inspired by the Safari Net concept from **MineFactory Reloaded** and continues the work of the original MobCapturer contributors.

---

## 🛡️ Protected mobs

Some mobs belong to other gameplay systems and must never be converted into MobCapturer eggs.

SF MobCapturer therefore **hard-blocks capture** of:

- **EliteMobs** elite entities, NPC entities, and super mobs
- **MythicMobs** active MythicMob entities

Detection uses the owning plugin's runtime API when available, with persistent-data and metadata safeguards as fallback checks. If one of these integrations is enabled but its ownership API cannot be queried reliably, MobCapturer **fails closed** and refuses capture rather than risk duplication, state loss, or corruption.

There is no player bypass for these protections.

---

## 📦 Requirements

| Requirement | Supported setup |
| --- | --- |
| **Slimefun core** | Slimefun Legacy 4.1.37+ |
| **Primary server** | Paper 26.2 |
| **Secondary server** | Purpur based on Paper 26.2 |
| **Folia** | Folia-aware; Slimefun Legacy's Folia support remains experimental |
| **Java runtime** | Java 25 |
| **Client** | Normal Minecraft Java client; no client mod required |

SF MobCapturer builds on Java 25 while targeting Java 21-compatible bytecode for plugin classes.

The fork no longer requires **GuizhanLibPlugin** at runtime.

---

## 🚀 Installation

1. Stop the server normally.
2. Back up the server before replacing an existing MobCapturer build.
3. Download the latest raw JAR from [GitHub Releases](https://github.com/wickidcow/SF_MobCapturer/releases).
4. Place the JAR in the server's `plugins` directory.
5. Remove the previous MobCapturer JAR so only one MobCapturer implementation loads.
6. Make sure **Slimefun Legacy** is installed.
7. Start the server and review the console for startup errors.

Do not use `/reload` when installing or updating Slimefun addons.

---

## 🔌 Compatibility

| Server software | Compatibility |
| --- | :---: |
| Paper 26.2 | ✅ Primary supported line |
| Purpur based on Paper 26.2 | ✅ Supported |
| Most conventional Paper forks | ⚠️ Usually compatible |
| Folia based on Paper 26.2 | ⚠️ Experimental stack support |
| Spigot | ❌ Unsupported |
| CraftBukkit / Bukkit | ❌ Unsupported |
| Hybrid servers | ❌ Unsupported |

### EliteMobs and MythicMobs

EliteMobs and MythicMobs are optional integrations. They are **not required** to run SF MobCapturer.

When either plugin is installed, MobCapturer checks entity ownership before capture. Managed mobs are rejected before any MobCapturer egg is created or the entity is removed.

### Existing MobCapturer eggs

SF MobCapturer retains compatibility handling for older captured-mob data. This includes recovery of affected Dev-37 eggs that stored Paper attribute objects using unstable `CraftAttribute{...}` text instead of stable namespaced attribute keys.

New captures store stable namespaced attribute identifiers such as:

```text
minecraft:max_health
minecraft:movement_speed
minecraft:fall_damage_multiplier
```

---

## ⚙️ Folia safety

SF MobCapturer's capture and release paths operate from entity/player-owned event contexts and do not schedule delayed world or entity mutations through Bukkit's global scheduler.

The fork also uses a Folia-capable bStats release and declares Folia support in its plugin metadata. The addon itself is kept region-local; however, **every plugin in a Folia server's stack must also be Folia-safe**. Slimefun Legacy currently treats Folia support as experimental, so production Folia deployments should be validated on a staging server first.

---

## 🧰 Building from source

SF MobCapturer is built with Gradle.

```text
./gradlew clean shadowJar
```

Release CI compiles against the exact Slimefun Legacy release target and outputs a raw JAR under:

```text
build/libs/SF_MobCapturer<version>.jar
```

---

## ❤️ Credits

SF MobCapturer exists because of the work of the original MobCapturer and Slimefun communities.

Special credit goes to:

- **TheBusyBiscuit** and the original MobCapturer contributors for the original project and gameplay concept
- **ybw0014** and later maintainers for continued MobCapturer compatibility work
- The **Slimefun community** for maintaining the addon ecosystem over the years
- **Slimefun Legacy** for preserving the classic Slimefun experience on modern Paper servers

This fork is maintained for [AlbionMC.com](https://albionmc.com) and the wider Slimefun community.

---

## ⚖️ License

This project remains available under the repository's [MIT License](LICENSE).

Contributions and bug reports are welcome through GitHub.
