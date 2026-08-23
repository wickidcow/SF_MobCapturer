# SF MobCapturer 1.0.1

Compatibility and safety update for the Slimefun Legacy edition of MobCapturer.

## Highlights

- Hard-blocks capture of **EliteMobs** elite entities, NPCs, and super mobs.
- Hard-blocks capture of active **MythicMobs** entities.
- Uses owning-plugin API checks with persistent-data and metadata safeguards.
- Fails closed if an enabled EliteMobs or MythicMobs integration cannot reliably identify managed mobs, preventing accidental conversion or duplication.
- Adds Folia-aware plugin metadata and removes legacy Bukkit scheduler assumptions from the supported capture/release paths.
- Updates bStats to the Folia-capable 3.1.x line.
- Removes the GuizhanLibPlugin runtime requirement and keeps the addon self-contained for its inherited compatibility helpers.
- Adds CI compilation gates for **Paper 26.2**, **Purpur 26.2**, and **Folia 26.2** APIs.
- Retains the Paper 26.2 attribute compatibility and Dev-37 captured-egg recovery introduced in 1.0.0.
- Replaces the inherited README with current Slimefun Legacy-focused installation, compatibility, safety, and credit information.
- Release asset is provided directly as `SF_MobCapturer1.0.1.jar`.

## Upgrade

Stop the server, replace the previous MobCapturer JAR with `SF_MobCapturer1.0.1.jar`, and start the server normally. Do not use `/reload`.

Existing captured mobs remain compatible. EliteMobs and MythicMobs are optional integrations; when installed, their managed entities are rejected before MobCapturer creates an egg or removes the entity.
