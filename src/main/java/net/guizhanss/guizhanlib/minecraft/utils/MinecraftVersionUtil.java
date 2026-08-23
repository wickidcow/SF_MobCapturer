package net.guizhanss.guizhanlib.minecraft.utils;

/**
 * Minimal source-compatibility shim retained for the upstream MobCapturer adapters.
 * SF MobCapturer targets Paper 26.2 / Minecraft 26.2, which is newer than every
 * historical version gate used by this codebase.
 */
public final class MinecraftVersionUtil {

    private MinecraftVersionUtil() {}

    public static boolean isAtLeast(int... version) {
        return true;
    }
}
