package io.github.thebusybiscuit.mobcapturer.utils.compatibility;

import java.util.Locale;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

import io.github.thebusybiscuit.mobcapturer.utils.ReflectionUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class VillagerProfessionX {

    public static String getFromZombieVillager(ZombieVillager entity) {
        var profession = ReflectionUtils.invoke(entity, "getVillagerProfession");
        if (profession == null) {
            return "UNKNOWN";
        }

        var nsKey = (NamespacedKey) ReflectionUtils.invoke(profession, "getKey");
        return nsKey.getKey().toUpperCase(Locale.ROOT);
    }

    public static Villager.Profession fromString(String value) {
        if (value == null || value.equalsIgnoreCase("Unknown")) {
            return null;
        }

        return Registry.VILLAGER_PROFESSION.get(
                NamespacedKey.minecraft(value.toLowerCase(Locale.ROOT))
        );
    }

    public static void setToZombieVillager(ZombieVillager entity, String obj) {
        Villager.Profession profession = fromString(obj);
        if (profession != null) {
            ReflectionUtils.invoke(entity, "setVillagerProfession", profession);
        }
    }
}
