package net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.entity;

import org.bukkit.entity.TropicalFish.Pattern;

public final class TropicalFishHelper {
    private TropicalFishHelper() {}
    public static String getPatternName(Pattern pattern) {
        return DisplayNameHelper.humanize(pattern.name());
    }
}
