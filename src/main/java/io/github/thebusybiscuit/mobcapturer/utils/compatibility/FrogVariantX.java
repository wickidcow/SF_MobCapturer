package io.github.thebusybiscuit.mobcapturer.utils.compatibility;

import java.util.Locale;

import org.bukkit.Keyed;
import org.bukkit.entity.Frog;

import io.github.thebusybiscuit.mobcapturer.utils.ReflectionUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class FrogVariantX {

    public static String get(Frog entity) {
        Object obj = ReflectionUtils.invoke(entity, "getVariant");

        if (obj instanceof Keyed keyed) {
            return keyed.getKey().getKey().toUpperCase(Locale.ROOT);
        }

        return normalize(obj != null ? obj.toString() : "TEMPERATE");
    }

    public static void set(Frog entity, String obj) {
        String variant = normalize(obj);
        Object value = ReflectionUtils.valueOf(Frog.Variant.class, variant);
        if (value != null) {
            ReflectionUtils.invoke(entity, "setVariant", value);
        }
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "TEMPERATE";
        }

        String value = raw.trim();
        int colon = value.lastIndexOf(':');
        if (colon >= 0 && colon + 1 < value.length()) {
            value = value.substring(colon + 1);
        }

        value = value.replaceAll("[^A-Za-z_]", "");
        String upper = value.toUpperCase(Locale.ROOT);

        if (upper.endsWith("WARM")) {
            return "WARM";
        }
        if (upper.endsWith("COLD")) {
            return "COLD";
        }
        if (upper.endsWith("TEMPERATE")) {
            return "TEMPERATE";
        }

        return "TEMPERATE";
    }
}
