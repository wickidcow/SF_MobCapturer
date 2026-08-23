package io.github.thebusybiscuit.mobcapturer.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.mobcapturer.MobCapturer;

/**
 * Detects entities managed by boss/mob plugins which must never be converted
 * into MobCapturer eggs.
 *
 * <p>Integrations deliberately fail closed: if EliteMobs or MythicMobs is
 * enabled but its ownership API cannot be queried reliably, capture is blocked
 * rather than risking corruption, duplication, or loss of plugin-managed state.</p>
 */
public final class ProtectedMobDetector {

    private static final String ELITEMOBS = "EliteMobs";
    private static final String MYTHICMOBS = "MythicMobs";

    private final MobCapturer plugin;
    private final EliteMobsBridge eliteMobs;
    private final MythicMobsBridge mythicMobs;

    public ProtectedMobDetector(@Nonnull MobCapturer plugin) {
        this.plugin = plugin;
        this.eliteMobs = new EliteMobsBridge(plugin);
        this.mythicMobs = new MythicMobsBridge(plugin);
    }

    /**
     * @return the external plugin name that owns the entity, or {@code null}
     *         when MobCapturer may handle it.
     */
    @Nullable
    public String getBlockingPlugin(@Nonnull LivingEntity entity) {
        if (eliteMobs.matches(entity)) {
            return ELITEMOBS;
        }

        if (mythicMobs.matches(entity)) {
            return MYTHICMOBS;
        }

        return null;
    }

    private static boolean hasNamespace(Entity entity, String namespace) {
        for (NamespacedKey key : entity.getPersistentDataContainer().getKeys()) {
            if (namespace.equalsIgnoreCase(key.getNamespace())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasMetadata(Entity entity, String... keys) {
        for (String key : keys) {
            if (entity.hasMetadata(key)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Method findMethod(Class<?> type, String name, Class<?> parameter) {
        try {
            return type.getMethod(name, parameter);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private abstract static class Bridge {

        protected final MobCapturer plugin;
        protected final Plugin external;
        private final String pluginName;
        private final AtomicBoolean runtimeWarning = new AtomicBoolean();
        protected boolean failClosed;

        protected Bridge(MobCapturer plugin, String pluginName) {
            this.plugin = plugin;
            this.pluginName = pluginName;
            this.external = plugin.getServer().getPluginManager().getPlugin(pluginName);
        }

        protected boolean isEnabled() {
            return external != null && external.isEnabled();
        }

        protected boolean failClosed() {
            if (!isEnabled()) {
                return false;
            }

            if (runtimeWarning.compareAndSet(false, true)) {
                plugin.getLogger().severe(pluginName + " is enabled but its mob ownership API could not be queried. "
                    + "Mob capture is blocked as a safety measure.");
            }
            return true;
        }

        protected void markUnavailable(Throwable throwable) {
            failClosed = true;
            plugin.getLogger().warning(pluginName + " integration could not be initialized ("
                + throwable.getClass().getSimpleName() + "). Capture will fail closed while " + pluginName + " is enabled.");
        }

        abstract boolean matches(LivingEntity entity);
    }

    private static final class EliteMobsBridge extends Bridge {

        private Method isElite;
        private Method isNpc;
        private Method isSuperMob;

        private EliteMobsBridge(MobCapturer plugin) {
            super(plugin, ELITEMOBS);
            if (!isEnabled()) {
                return;
            }

            try {
                Class<?> tagger = Class.forName(
                    "com.magmaguy.elitemobs.tagger.PersistentTagger",
                    false,
                    external.getClass().getClassLoader()
                );
                isElite = findMethod(tagger, "isEliteEntity", Entity.class);
                isNpc = findMethod(tagger, "isNPC", Entity.class);
                isSuperMob = findMethod(tagger, "isSuperMob", Entity.class);

                if (isElite == null && isNpc == null && isSuperMob == null) {
                    failClosed = true;
                }
            } catch (ClassNotFoundException | LinkageError ex) {
                markUnavailable(ex);
            }
        }

        @Override
        boolean matches(LivingEntity entity) {
            // PersistentTagger uses the EliteMobs namespace for its managed entity markers.
            if (hasNamespace(entity, "elitemobs")
                || hasMetadata(entity, "EliteEntity", "EliteMob", "NPCEntity", "SuperMob")) {
                return true;
            }

            if (!isEnabled()) {
                return false;
            }

            if (failClosed) {
                return failClosed();
            }

            try {
                return invokeBoolean(isElite, null, entity)
                    || invokeBoolean(isNpc, null, entity)
                    || invokeBoolean(isSuperMob, null, entity);
            } catch (ReflectiveOperationException | LinkageError ex) {
                markUnavailable(ex);
                return true;
            }
        }
    }

    private static final class MythicMobsBridge extends Bridge {

        private Object mobManager;
        private Method isMythicMobEntity;
        private Method isActiveMobEntity;
        private Method isActiveMobUuid;
        private Method getActiveMobUuid;

        private MythicMobsBridge(MobCapturer plugin) {
            super(plugin, MYTHICMOBS);
            if (!isEnabled()) {
                return;
            }

            try {
                Class<?> mythicBukkit = Class.forName(
                    "io.lumine.mythic.bukkit.MythicBukkit",
                    false,
                    external.getClass().getClassLoader()
                );
                Object instance = mythicBukkit.getMethod("inst").invoke(null);
                mobManager = instance.getClass().getMethod("getMobManager").invoke(instance);

                isMythicMobEntity = findMethod(mobManager.getClass(), "isMythicMob", Entity.class);
                isActiveMobEntity = findMethod(mobManager.getClass(), "isActiveMob", Entity.class);
                isActiveMobUuid = findMethod(mobManager.getClass(), "isActiveMob", UUID.class);
                getActiveMobUuid = findMethod(mobManager.getClass(), "getActiveMob", UUID.class);

                if (isMythicMobEntity == null && isActiveMobEntity == null
                    && isActiveMobUuid == null && getActiveMobUuid == null) {
                    failClosed = true;
                }
            } catch (ReflectiveOperationException | LinkageError ex) {
                markUnavailable(ex);
            }
        }

        @Override
        boolean matches(LivingEntity entity) {
            if (hasNamespace(entity, "mythicmobs")
                || hasMetadata(entity, "MythicMob", "mythicmob", "MYTHIC_MOB")) {
                return true;
            }

            if (!isEnabled()) {
                return false;
            }

            if (failClosed) {
                return failClosed();
            }

            try {
                if (invokeBoolean(isMythicMobEntity, mobManager, entity)
                    || invokeBoolean(isActiveMobEntity, mobManager, entity)
                    || invokeBoolean(isActiveMobUuid, mobManager, entity.getUniqueId())) {
                    return true;
                }

                if (getActiveMobUuid != null) {
                    Object result = getActiveMobUuid.invoke(mobManager, entity.getUniqueId());
                    if (result instanceof java.util.Optional<?> optional) {
                        return optional.isPresent();
                    }
                    return result != null;
                }

                return false;
            } catch (ReflectiveOperationException | LinkageError ex) {
                markUnavailable(ex);
                return true;
            }
        }
    }

    private static boolean invokeBoolean(@Nullable Method method, @Nullable Object target, Object argument)
        throws InvocationTargetException, IllegalAccessException {
        return method != null && Boolean.TRUE.equals(method.invoke(target, argument));
    }
}
