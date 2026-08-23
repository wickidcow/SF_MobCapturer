package net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.entity;

import java.util.Locale;

import org.bukkit.entity.EntityType;

/**
 * Minimal entity-name helper retained for source compatibility with inherited
 * MobCapturer setup code.
 */
public final class EntityTypeHelper {

    private EntityTypeHelper() {}

    public static String getName(EntityType type) {
        String[] parts = type.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
