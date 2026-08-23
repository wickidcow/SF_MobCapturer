package net.guizhanss.minecraft.guizhanlib.gugu.java;

/**
 * Minimal display helper retained for source compatibility with inherited adapters.
 */
public final class BooleanHelper {

    private BooleanHelper() {}

    public static String yesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
