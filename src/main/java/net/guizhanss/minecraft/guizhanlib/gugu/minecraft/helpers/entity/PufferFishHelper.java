package net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.entity;

public final class PufferFishHelper {
    private PufferFishHelper() {}
    public static String getPuffState(int value) {
        return switch (value) {
            case 0 -> "Deflated";
            case 1 -> "Half Puffed";
            case 2 -> "Fully Puffed";
            default -> Integer.toString(value);
        };
    }
}
