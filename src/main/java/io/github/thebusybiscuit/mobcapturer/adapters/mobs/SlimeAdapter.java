package io.github.thebusybiscuit.mobcapturer.adapters.mobs;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;

import org.bukkit.ChatColor;
import org.bukkit.entity.Mob;

import io.github.thebusybiscuit.mobcapturer.adapters.MobAdapter;

/**
 * Stores cube-mob size without depending on either the old Slime inheritance tree
 * or the newer AbstractCubeMob API. This keeps one JAR compatible with 1.21.11 and 26.2.
 */
public class SlimeAdapter<T extends Mob> implements MobAdapter<T> {

    private final Class<T> entityClass;

    public SlimeAdapter(@Nonnull Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Nonnull
    @Override
    public List<String> getLore(@Nonnull JsonObject json) {
        List<String> lore = MobAdapter.super.getLore(json);
        if (json.has("size")) {
            lore.add(ChatColor.GRAY + "Size: " + ChatColor.WHITE + json.get("size").getAsInt());
        }
        return lore;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void apply(T entity, JsonObject json) {
        if (json.has("size")) {
            invokeSizeSetter(entity, json.get("size").getAsInt());
        }
        MobAdapter.super.apply(entity, json);
    }

    @Nonnull
    @Override
    public JsonObject saveData(@Nonnull T entity) {
        JsonObject json = MobAdapter.super.saveData(entity);
        Integer size = invokeSizeGetter(entity);
        if (size != null) {
            json.addProperty("size", size);
        }
        return json;
    }

    @Nonnull
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    private static Integer invokeSizeGetter(Mob entity) {
        try {
            Method method = entity.getClass().getMethod("getSize");
            Object value = method.invoke(entity);
            return value instanceof Number number ? number.intValue() : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static void invokeSizeSetter(Mob entity, int size) {
        try {
            Method method = entity.getClass().getMethod("setSize", int.class);
            method.invoke(entity, size);
        } catch (ReflectiveOperationException ignored) {
            // A future cube implementation without a size setter simply keeps its default size.
        }
    }
}
