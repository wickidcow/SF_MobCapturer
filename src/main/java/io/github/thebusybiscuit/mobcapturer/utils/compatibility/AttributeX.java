package io.github.thebusybiscuit.mobcapturer.utils.compatibility;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

/**
 * Compatibility helpers for Bukkit/Paper's registry-backed attribute API.
 *
 * <p>MobCapturer historically stored {@code Attribute#toString()} values. On modern
 * Paper that can be an implementation string such as {@code CraftAttribute{...}},
 * which cannot be passed back through the old enum-style valueOf path. New captures
 * are stored with stable namespaced registry keys, while old values are normalized
 * on read.</p>
 */
public final class AttributeX {

    private static final Pattern CRAFT_ATTRIBUTE_KEY = Pattern.compile(
        "MINECRAFT:ATTRIBUTE\\s*/\\s*MINECRAFT:([A-Z0-9_./-]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final String[] LEGACY_PREFIXES = {
        "GENERIC_", "PLAYER_", "HORSE_", "ZOMBIE_"
    };

    private AttributeX() {
    }

    @Nonnull
    public static String getKey(@Nonnull Attribute attribute) {
        return attribute.getKey().toString();
    }

    /**
     * Resolves modern namespaced keys, legacy enum names and Paper CraftAttribute
     * implementation strings written by affected MobCapturer builds.
     */
    @Nullable
    public static Attribute valueOf(@Nonnull String name) {
        String normalized = normalizeAttributeKey(name);
        if (normalized == null) {
            return null;
        }

        NamespacedKey key = NamespacedKey.fromString(normalized);
        if (key == null) {
            return null;
        }

        return Registry.ATTRIBUTE.get(key);
    }

    @Nullable
    static String normalizeAttributeKey(@Nonnull String rawName) {
        String name = rawName.trim();
        if (name.isEmpty()) {
            return null;
        }

        Matcher matcher = CRAFT_ATTRIBUTE_KEY.matcher(name);
        if (matcher.find()) {
            return "minecraft:" + matcher.group(1).toLowerCase(Locale.ROOT);
        }

        String normalized = name.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("minecraft:")) {
            return normalized;
        }

        if (normalized.indexOf(':') >= 0) {
            return normalized;
        }

        String legacyName = name.toUpperCase(Locale.ROOT);
        for (String prefix : LEGACY_PREFIXES) {
            if (legacyName.startsWith(prefix)) {
                legacyName = legacyName.substring(prefix.length());
                break;
            }
        }

        return "minecraft:" + legacyName.toLowerCase(Locale.ROOT);
    }

    @Nonnull
    public static JsonObject serializeAttributesFromEntity(@Nonnull LivingEntity entity) {
        JsonObject attributes = new JsonObject();

        for (Attribute attribute : Registry.ATTRIBUTE) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                continue;
            }

            JsonObject attributeObj = new JsonObject();
            attributeObj.addProperty("base", instance.getBaseValue());

            JsonArray modifiers = new JsonArray();
            for (AttributeModifier modifier : instance.getModifiers()) {
                JsonObject mod = new JsonObject();
                Map<String, Object> serializedMod = modifier.serialize();

                for (Map.Entry<String, Object> entry : serializedMod.entrySet()) {
                    mod.addProperty(entry.getKey(), entry.getValue().toString());
                }

                modifiers.add(mod);
            }

            attributeObj.add("modifiers", modifiers);
            attributes.add(getKey(attribute), attributeObj);
        }

        return attributes;
    }
}
