package io.github.thebusybiscuit.mobcapturer.adapters.mobs;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;

/**
 * Reflection-backed Sulfur Cube adapter. The class intentionally has no compile-time
 * dependency on the 26.2 SulfurCube API so the same JAR can load on 1.21.11.
 */
public class SulfurCubeAdapter extends RuntimeMobAdapter {

    public SulfurCubeAdapter(@Nonnull Class<LivingEntity> entityClass) {
        super(entityClass);
    }

    @Nonnull
    @Override
    public List<String> getLore(@Nonnull JsonObject json) {
        List<String> lore = super.getLore(json);
        if (json.has("size")) {
            lore.add(ChatColor.GRAY + "Size: " + ChatColor.WHITE + json.get("size").getAsInt());
        }
        if (json.has("canWander")) {
            lore.add(ChatColor.GRAY + "Can Wander: " + ChatColor.WHITE + json.get("canWander").getAsBoolean());
        }
        if (json.has("canExplode")) {
            lore.add(ChatColor.GRAY + "Can Explode: " + ChatColor.WHITE + json.get("canExplode").getAsBoolean());
        }
        if (json.has("fuseTicks")) {
            lore.add(ChatColor.GRAY + "Fuse Ticks: " + ChatColor.WHITE + json.get("fuseTicks").getAsInt());
        }
        return lore;
    }

    @Nonnull
    @Override
    public JsonObject saveData(@Nonnull LivingEntity entity) {
        JsonObject json = super.saveData(entity);
        putNumber(json, "size", invoke(entity, "getSize"));
        putBoolean(json, "canWander", invoke(entity, "canWander"));
        putBoolean(json, "canExplode", invoke(entity, "canExplode"));
        putNumber(json, "fuseTicks", invoke(entity, "getFuseTicks"));
        return json;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void apply(LivingEntity entity, JsonObject json) {
        // Size resets cube attributes, so restore it before applying the common MobAdapter state.
        if (json.has("size")) {
            invoke(entity, "setSize", int.class, json.get("size").getAsInt());
        }
        if (json.has("canWander")) {
            invoke(entity, "setWander", boolean.class, json.get("canWander").getAsBoolean());
        }

        super.apply(entity, json);

        if (json.has("fuseTicks")) {
            invoke(entity, "setFuseTicks", int.class, json.get("fuseTicks").getAsInt());
        }
    }

    private static Object invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void invoke(Object target, String methodName, Class<?> parameterType, Object value) {
        try {
            Method method = target.getClass().getMethod(methodName, parameterType);
            method.invoke(target, value);
        } catch (ReflectiveOperationException ignored) {
            // The optional state is simply skipped on servers where the method is unavailable.
        }
    }

    private static void putNumber(JsonObject json, String key, Object value) {
        if (value instanceof Number number) {
            json.addProperty(key, number);
        }
    }

    private static void putBoolean(JsonObject json, String key, Object value) {
        if (value instanceof Boolean bool) {
            json.addProperty(key, bool);
        }
    }
}
