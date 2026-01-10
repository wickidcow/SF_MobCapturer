package io.github.thebusybiscuit.mobcapturer.adapters.mobs;

import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ZombieNautilus;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

public class ZombieNautilusAdapter extends AbstractNautilusAdapter<ZombieNautilus> {

    public ZombieNautilusAdapter() {
        super(ZombieNautilus.class);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void apply(ZombieNautilus entity, JsonObject json) {
        super.apply(entity, json);

        try {
            ZombieNautilus.Variant variant = RegistryAccess.registryAccess().getRegistry(RegistryKey.ZOMBIE_NAUTILUS_VARIANT).getOrThrow(NamespacedKey.minecraft(json.get("variant").getAsString()));
            entity.setVariant(variant);
        } catch (NoSuchElementException ignored) {
            // ignored
        }
    }

    @Nonnull
    @Override
    public JsonObject saveData(@Nonnull ZombieNautilus entity) {
        JsonObject json = super.saveData(entity);

        json.addProperty("variant", entity.getVariant().getKey().getKey());

        return json;
    }
}
