package io.github.thebusybiscuit.mobcapturer.diagnostics;

import io.github.thebusybiscuit.mobcapturer.MobCapturer;
import io.github.thebusybiscuit.mobcapturer.items.MobEgg;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Item-local Doctor migration support for historical translated Mob Egg presentation. */
public final class MobEggPresentationMigration {

    public static final String CANDIDATE_TYPE = "legacy-cjk-mob-egg-presentation";
    private static final String PREFIX = "MOB_EGG_";

    private MobEggPresentationMigration() {}

    public static @Nonnull Set<String> supportedItemIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (EntityType type : MobCapturer.getRegistry().getAdapters().keySet()) {
            ids.add(PREFIX + type.name());
        }
        return Set.copyOf(ids);
    }

    public static @Nullable Result inspect(@Nonnull ItemStack item, @Nonnull String slimefunId) {
        MobEgg<?> egg = resolveEgg(slimefunId);
        if (egg == null || !containsCjkPresentation(item)) {
            return null;
        }

        String claim = egg.getPresentationClaim(item);
        if (claim == null) {
            // Empty/base Mob Eggs do not contain captured mob state. Core presentation repair can
            // safely use the registered English template for those and no schema migration is needed.
            return null;
        }

        return new Result(
                CANDIDATE_TYPE,
                claim,
                "Captured Mob Egg has translated legacy presentation; entity PDC can regenerate English lore.");
    }

    public static boolean migrate(
            @Nonnull ItemStack item,
            @Nonnull String slimefunId,
            @Nonnull String candidateType,
            @Nonnull String validationClaim) {
        if (!CANDIDATE_TYPE.equals(candidateType)) {
            return false;
        }

        MobEgg<?> egg = resolveEgg(slimefunId);
        if (egg == null) {
            return false;
        }

        String currentClaim = egg.getPresentationClaim(item);
        if (currentClaim == null || !currentClaim.equals(validationClaim)) {
            return false;
        }

        return egg.refreshPresentation(item);
    }

    private static @Nullable MobEgg<?> resolveEgg(@Nonnull String slimefunId) {
        if (!slimefunId.startsWith(PREFIX) || slimefunId.length() <= PREFIX.length()) {
            return null;
        }

        String entityName = slimefunId.substring(PREFIX.length()).toUpperCase(Locale.ROOT);
        try {
            EntityType type = EntityType.valueOf(entityName);
            return MobCapturer.getRegistry().getAdapters().get(type);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean containsCjkPresentation(@Nonnull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && containsCjk(meta.getDisplayName())) {
            return true;
        }
        List<String> lore = meta.hasLore() ? meta.getLore() : null;
        if (lore != null) {
            for (String line : lore) {
                if (containsCjk(line)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsCjk(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
            if (script == Character.UnicodeScript.HAN
                    || script == Character.UnicodeScript.HIRAGANA
                    || script == Character.UnicodeScript.KATAKANA
                    || script == Character.UnicodeScript.HANGUL) {
                return true;
            }
            offset += Character.charCount(codePoint);
        }
        return false;
    }

    public record Result(@Nonnull String candidateType, @Nonnull String validationClaim, @Nonnull String detail) {}
}
