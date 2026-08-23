package net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.entity;

import java.util.Locale;

final class DisplayNameHelper {

    private DisplayNameHelper() {}

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
