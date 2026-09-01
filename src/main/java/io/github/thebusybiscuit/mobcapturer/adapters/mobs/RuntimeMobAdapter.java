package io.github.thebusybiscuit.mobcapturer.adapters.mobs;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;

import org.bukkit.entity.LivingEntity;

import io.github.thebusybiscuit.mobcapturer.adapters.MobAdapter;

/**
 * Generic runtime adapter for mob classes that do not exist on the 1.21.11 compile baseline.
 */
public class RuntimeMobAdapter implements MobAdapter<LivingEntity> {

    private final Class<LivingEntity> entityClass;

    public RuntimeMobAdapter(@Nonnull Class<LivingEntity> entityClass) {
        this.entityClass = entityClass;
    }

    @Nonnull
    @Override
    public Class<LivingEntity> getEntityClass() {
        return entityClass;
    }

    @Nonnull
    @Override
    public JsonObject saveData(@Nonnull LivingEntity entity) {
        return MobAdapter.super.saveData(entity);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void apply(LivingEntity entity, JsonObject json) {
        MobAdapter.super.apply(entity, json);
    }
}
