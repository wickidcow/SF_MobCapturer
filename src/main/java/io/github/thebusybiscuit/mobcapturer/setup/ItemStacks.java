package io.github.thebusybiscuit.mobcapturer.setup;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;

import net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.entity.EntityTypeHelper;

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
        "&6生物捕捉枪",
        "",
        "&e右键点击&7射出一枚&f生物捕捉弹"
    );
    public static final SlimefunItemStack MOB_CAPTURING_PELLET = new SlimefunItemStack(
        "MOB_CAPTURING_PELLET",
        "983b30e9d135b05190eea2c3ac61e2ab55a2d81e1a58dbb26983a14082664",
        "&f生物捕捉弹",
        "",
        "&7是&6生物捕捉枪&7的弹药"
    );

    @Nonnull
    @ParametersAreNonnullByDefault
    public static SlimefunItemStack buildMobEgg(EntityType type, String eggTexture) {
        Preconditions.checkArgument(type != null, "Entity type cannot be null");
        Preconditions.checkArgument(eggTexture != null, "Egg texture cannot be null");

        return new SlimefunItemStack(
            "MOB_EGG_" + type,
            eggTexture,
            "&a刷怪蛋 &7(" + EntityTypeHelper.getName(type) + ")",
            "",
            "&7对着方块右键点击此物品",
            "&7即可释放捕捉的生物"
        );
    }
}
