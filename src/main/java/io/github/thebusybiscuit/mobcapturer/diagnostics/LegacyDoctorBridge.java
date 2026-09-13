package io.github.thebusybiscuit.mobcapturer.diagnostics;

import io.github.thebusybiscuit.mobcapturer.MobCapturer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

/** Reflective bridge to Slimefun Legacy's optional same-ID Doctor migration APIs. */
public final class LegacyDoctorBridge {

    private static final String SCHEMA_PROBE_API =
            "io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaProbe";
    private static final String SCHEMA_CANDIDATE_API =
            "io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaCandidate";
    private static final String SCHEMA_READINESS_API =
            "io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaCandidate$Readiness";
    private static final String SCHEMA_MIGRATOR_API =
            "io.github.thebusybiscuit.slimefun4.api.diagnostics.LegacyItemSchemaMigrator";

    private LegacyDoctorBridge() {}

    public static void register(MobCapturer plugin) {
        Plugin slimefun = Bukkit.getPluginManager().getPlugin("Slimefun");
        if (slimefun == null) {
            return;
        }

        ClassLoader loader = slimefun.getClass().getClassLoader();
        registerSchemaProbe(plugin, loader);
        registerSchemaMigrator(plugin, loader);
    }

    public static void unregister(MobCapturer plugin) {
        Bukkit.getServicesManager().unregisterAll(plugin);
    }

    private static void registerSchemaProbe(MobCapturer plugin, ClassLoader loader) {
        try {
            Class<?> probeInterface = Class.forName(SCHEMA_PROBE_API, false, loader);
            Class<?> candidateClass = Class.forName(SCHEMA_CANDIDATE_API, false, loader);
            Class<?> readinessClass = Class.forName(SCHEMA_READINESS_API, false, loader);
            Constructor<?> candidateConstructor;
            boolean supportsValidationClaim;
            try {
                candidateConstructor = candidateClass.getConstructor(
                        String.class, readinessClass, String.class, String.class);
                supportsValidationClaim = true;
            } catch (NoSuchMethodException ignored) {
                candidateConstructor = candidateClass.getConstructor(String.class, readinessClass, String.class);
                supportsValidationClaim = false;
            }

            Method readinessValueOf = readinessClass.getMethod("valueOf", String.class);
            Constructor<?> finalConstructor = candidateConstructor;
            boolean finalSupportsClaim = supportsValidationClaim;
            InvocationHandler handler = (proxy, method, arguments) -> invokeSchemaProbe(
                    proxy, method, arguments, finalConstructor, readinessValueOf, finalSupportsClaim);
            Object provider = Proxy.newProxyInstance(loader, new Class<?>[] {probeInterface}, handler);
            registerRaw(Bukkit.getServicesManager(), probeInterface, provider, plugin);
            plugin.getLogger().info("Registered MobCapturer legacy Mob Egg presentation probe with Slimefun Doctor.");
        } catch (ClassNotFoundException ignored) {
            // Other Slimefun implementations do not necessarily expose Legacy's optional migration API.
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING, "Could not register the optional Slimefun Mob Egg schema probe.", exception);
        }
    }

    private static void registerSchemaMigrator(MobCapturer plugin, ClassLoader loader) {
        try {
            Class<?> migratorInterface = Class.forName(SCHEMA_MIGRATOR_API, false, loader);
            InvocationHandler handler = LegacyDoctorBridge::invokeSchemaMigrator;
            Object provider = Proxy.newProxyInstance(loader, new Class<?>[] {migratorInterface}, handler);
            registerRaw(Bukkit.getServicesManager(), migratorInterface, provider, plugin);
            plugin.getLogger().info("Registered MobCapturer Mob Egg presentation migrator with Slimefun Doctor.");
        } catch (ClassNotFoundException ignored) {
            // The mutator API is optional and only exists on newer Slimefun Legacy builds.
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING, "Could not register the optional Slimefun Mob Egg schema migrator.", exception);
        }
    }

    private static Object invokeSchemaProbe(
            Object proxy,
            Method method,
            Object[] arguments,
            Constructor<?> candidateConstructor,
            Method readinessValueOf,
            boolean supportsValidationClaim)
            throws ReflectiveOperationException {
        return switch (method.getName()) {
            case "getMigrationName" -> "MobCapturer translated Mob Egg presentation";
            case "getSupportedItemIds" -> MobEggPresentationMigration.supportedItemIds();
            case "probeItem" -> {
                if (arguments == null
                        || arguments.length < 2
                        || !(arguments[0] instanceof ItemStack item)
                        || !(arguments[1] instanceof String slimefunId)) {
                    yield null;
                }

                MobEggPresentationMigration.Result result = MobEggPresentationMigration.inspect(item, slimefunId);
                if (result == null) {
                    yield null;
                }

                if (supportsValidationClaim) {
                    Object ready = readinessValueOf.invoke(null, "READY");
                    yield candidateConstructor.newInstance(
                            result.candidateType(), ready, result.detail(), result.validationClaim());
                }

                Object manual = readinessValueOf.invoke(null, "MANUAL_ONLY");
                yield candidateConstructor.newInstance(
                        result.candidateType(),
                        manual,
                        "Translated captured Mob Egg detected; this Slimefun build cannot authorize item-local migration.");
            }
            case "toString" -> "MobCapturerLegacyItemSchemaProbe";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> arguments != null && arguments.length == 1 && arguments[0] == proxy;
            default -> throw new UnsupportedOperationException(
                    "Unsupported LegacyItemSchemaProbe method: " + method.getName());
        };
    }

    private static Object invokeSchemaMigrator(Object proxy, Method method, Object[] arguments) {
        return switch (method.getName()) {
            case "getSupportedCandidateTypes" -> Set.of(MobEggPresentationMigration.CANDIDATE_TYPE);
            case "migrateItem" -> {
                if (arguments == null
                        || arguments.length < 5
                        || !(arguments[0] instanceof ItemStack item)
                        || !(arguments[1] instanceof String slimefunId)
                        || !(arguments[2] instanceof String candidateType)
                        || !(arguments[3] instanceof String validationClaim)) {
                    yield false;
                }
                yield MobEggPresentationMigration.migrate(item, slimefunId, candidateType, validationClaim);
            }
            case "toString" -> "MobCapturerLegacyItemSchemaMigrator";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> arguments != null && arguments.length == 1 && arguments[0] == proxy;
            default -> throw new UnsupportedOperationException(
                    "Unsupported LegacyItemSchemaMigrator method: " + method.getName());
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerRaw(ServicesManager services, Class service, Object provider, MobCapturer plugin) {
        services.register(service, provider, plugin, ServicePriority.Normal);
    }
}
