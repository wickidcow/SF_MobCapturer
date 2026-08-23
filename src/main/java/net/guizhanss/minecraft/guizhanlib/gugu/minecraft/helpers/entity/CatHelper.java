package net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.entity;

public final class CatHelper {
    private CatHelper() {}
    public static String getTypeName(String value) {
        return DisplayNameHelper.humanize(value);
    }
}
