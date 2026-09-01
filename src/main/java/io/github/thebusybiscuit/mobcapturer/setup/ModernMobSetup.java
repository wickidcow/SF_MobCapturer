package io.github.thebusybiscuit.mobcapturer.setup;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.mobcapturer.MobCapturer;
import io.github.thebusybiscuit.mobcapturer.adapters.MobAdapter;
import io.github.thebusybiscuit.mobcapturer.adapters.mobs.CopperGolemAdapter;
import io.github.thebusybiscuit.mobcapturer.adapters.mobs.SulfurCubeAdapter;
import io.github.thebusybiscuit.mobcapturer.items.MobEgg;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

/**
 * Registers Minecraft mobs that were added after MobCapturer's original upstream
 * support window while keeping the release compatible with the 1.21.11 API baseline.
 */
public final class ModernMobSetup {

    private static final String COPPER_GOLEM_TEXTURE = "1c87b3836ca4798fc52d763f296af9be9645240177d7a17b0dc29503dbf2463b";
    private static final String SULFUR_CUBE_TEXTURE = "f0d9056ec6db388af12304ef96ffdc8228dcf368ab255323258b716f990b4ab";

    private ModernMobSetup() {}

    public static void setup() {
        registerMob(EntityType.COPPER_GOLEM, new CopperGolemAdapter(), COPPER_GOLEM_TEXTURE);
        registerSulfurCubeIfAvailable();
    }

    @SuppressWarnings("unchecked")
    private static void registerSulfurCubeIfAvailable() {
        final EntityType type;
        try {
            type = EntityType.valueOf("SULFUR_CUBE");
        } catch (IllegalArgumentException ignored) {
            // Sulfur Cube does not exist before Minecraft 26.2.
            return;
        }

        Class<? extends Entity> rawClass = type.getEntityClass();
        if (rawClass == null || !LivingEntity.class.isAssignableFrom(rawClass)) {
            MobCapturer.getInstance().getLogger().warning("Sulfur Cube exists but is not exposed as a LivingEntity; skipping capture registration.");
            return;
        }

        Class<LivingEntity> entityClass = (Class<LivingEntity>) rawClass;
        registerMob(type, new SulfurCubeAdapter(entityClass), SULFUR_CUBE_TEXTURE);
    }

    @ParametersAreNonnullByDefault
    private static <T extends LivingEntity> void registerMob(EntityType type, MobAdapter<T> adapter, String eggTexture) {
        String name = friendlyEntityName(type);

        MobEgg<T> egg = new MobEgg<>(
            ItemGroups.MOB_EGGS,
            ItemStacks.buildMobEgg(type, eggTexture),
            adapter,
            RecipeTypes.MOB_CAPTURING,
            new ItemStack[] {
                null, null, null,
                null, new CustomItemStack(SlimefunUtils.getCustomHead(eggTexture), ChatColor.WHITE + name), null,
                null, null, null
            }
        );

        egg.register(MobCapturer.getInstance());

        if (!egg.isDisabled()) {
            Researches.MOB_CAPTURING.addItems(egg);
            MobCapturer.getRegistry().getAdapters().put(type, egg);
        }
    }

    private static String friendlyEntityName(EntityType type) {
        String[] words = type.name().toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder output = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!output.isEmpty()) output.append(' ');
            output.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return output.toString();
    }
}
