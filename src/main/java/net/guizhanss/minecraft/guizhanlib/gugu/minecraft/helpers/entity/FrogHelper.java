package net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.entity;

public final class FrogHelper {
    private FrogHelper() {}
    public static String getVariantName(String value) {
        return DisplayNameHelper.humanize(value);
    }
}
