package io.github.thebusybiscuit.mobcapturer.utils.compatibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import io.github.thebusybiscuit.mobcapturer.MobCapturer;
import io.github.thebusybiscuit.mobcapturer.utils.ReflectionUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class AttributeX {

    private static final List<Object> allAttributes = new ArrayList<>();

    static {
        try {
            final var registry = ReflectionUtils.valueOf(Registry.class, "ATTRIBUTE");
            if (registry != null) {
                for (Object attribute : (Iterable<?>) registry) {
                    allAttributes.add(attribute);
                }
            }
        } catch (Exception ex) {
            MobCapturer.getInstance().getLogger().log(Level.SEVERE, "Failed to load attributes", ex);
        }
    }

    @Nonnull
    public static String getKey(@Nonnull Object attrObj) {
        NamespacedKey nsKey = (NamespacedKey) ReflectionUtils.invoke(attrObj, "getKey");
        return nsKey.toString();
    }

    @Nullable
    public static Attribute valueOf(@Nonnull String name) {
        // first attempt, call valueOf directly
        try {
            Attribute attr1 = (Attribute) ReflectionUtils.valueOf(Attribute.class, name);
            if (attr1 != null) {
                return attr1;
            }
        } catch (Exception ignored) {
            // first attempt failed, try second attempt
        }

        // second attempt, find by NamespacedKey
        for (var attrObj : allAttributes) {
            if (getKey(attrObj).equalsIgnoreCase(name)) {
                return (Attribute) attrObj;
            }
        }
        return null;
    }

    @Nonnull
    public static JsonObject serializeAttributesFromEntity(@Nonnull LivingEntity entity) {
        JsonObject attributes = new JsonObject();

        for (var attr : allAttributes) {
            AttributeInstance instance = entity.getAttribute((Attribute) attr);
            if (instance != null) {
                JsonObject attributeObj = new JsonObject();
                attributeObj.addProperty("base", instance.getBaseValue());

                JsonArray modifiers = new JsonArray();

                for (AttributeModifier modifier : instance.getModifiers()) {
                    JsonObject mod = new JsonObject();
                    Map<String, Object> serializedMod = modifier.serialize();

                    for (var entry : serializedMod.entrySet()) {
                        mod.addProperty(entry.getKey(), entry.getValue().toString());
                    }

                    modifiers.add(mod);
                }

                attributeObj.add("modifiers", modifiers);

                attributes.add(getKey(attr), attributeObj);
            }
        }

        return attributes;
    }
}
