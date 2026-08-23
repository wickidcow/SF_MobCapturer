package net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers;

import java.util.Locale;

/** Minimal display-name helper retained for inherited adapter source compatibility. */
public final class DyeColorHelper {

    private DyeColorHelper() {}

    public static String getName(String value) {
        return humanize(value);
    }

    static String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        StringBuilder result = new StringBuilder();
        for (String part : value.toLowerCase(Locale.ROOT).split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }
}
