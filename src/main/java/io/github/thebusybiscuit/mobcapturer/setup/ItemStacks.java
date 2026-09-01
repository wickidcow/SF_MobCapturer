package io.github.thebusybiscuit.mobcapturer.setup;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;

import lombok.experimental.UtilityClass;

/**
 * All the {@link SlimefunItemStack}s in MobCapturer.
 *
 * @author TheBusyBiscuit
 * @author ybw0014
 */
@UtilityClass
public final class ItemStacks {

    public static final SlimefunItemStack MOB_CANNON = new SlimefunItemStack(
        "MOB_CANNON",
        Material.BLAZE_ROD,
        "&6Mob Capturing Cannon",
        "",
        "&eRight-click &7to fire a &fMob Capturing Pellet"
    );
    public static final SlimefunItemStack MOB_CAPTURING_PELLET = new SlimefunItemStack(
        "MOB_CAPTURING_PELLET",
        "983b30e9d135b05190eea2c3ac61e2ab55a2d81e1a58dbb26983a14082664",
        "&fMob Capturing Pellet",
        "",
        "&7Ammunition for the &6Mob Capturing Cannon"
    );

    @Nonnull
    @ParametersAreNonnullByDefault
    public static SlimefunItemStack buildMobEgg(EntityType type, String eggTexture) {
        Preconditions.checkArgument(type != null, "Entity type cannot be null");
        Preconditions.checkArgument(eggTexture != null, "Egg texture cannot be null");

        return new SlimefunItemStack(
            "MOB_EGG_" + type,
            eggTexture,
            "&aMob Egg &7(" + friendlyEntityName(type) + ")",
            "",
            "&7Right-click a block with this item",
            "&7to release the captured mob"
        );
    }

    private static String friendlyEntityName(EntityType type) {
        String[] words = type.name().toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }

        return result.toString();
    }
}
