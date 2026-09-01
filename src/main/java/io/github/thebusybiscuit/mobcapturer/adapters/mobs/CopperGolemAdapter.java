package io.github.thebusybiscuit.mobcapturer.adapters.mobs;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;

import org.bukkit.ChatColor;
import org.bukkit.entity.CopperGolem;

import io.papermc.paper.world.WeatheringCopperState;

/**
 * Preserves the Copper Golem state introduced in the Copper Age update.
 */
public class CopperGolemAdapter extends StandardMobAdapter<CopperGolem> {

    public CopperGolemAdapter() {
        super(CopperGolem.class);
    }

    @Nonnull
    @Override
    public java.util.List<String> getLore(@Nonnull JsonObject json) {
        java.util.List<String> lore = super.getLore(json);
        lore.add(ChatColor.GRAY + "Weathering: " + ChatColor.WHITE + humanize(json.get("weathering").getAsString()));
        lore.add(ChatColor.GRAY + "State: " + ChatColor.WHITE + humanize(json.get("golemState").getAsString()));
        lore.add(ChatColor.GRAY + "Waxed: " + ChatColor.WHITE + "waxed".equals(json.get("oxidizingType").getAsString()));
        return lore;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void apply(CopperGolem entity, JsonObject json) {
        super.apply(entity, json);

        if (json.has("weathering")) {
            entity.setWeatheringState(WeatheringCopperState.valueOf(json.get("weathering").getAsString()));
        }
        if (json.has("golemState")) {
            entity.setGolemState(CopperGolem.State.valueOf(json.get("golemState").getAsString()));
        }
        if (json.has("oxidizingType")) {
            String type = json.get("oxidizingType").getAsString();
            if ("waxed".equals(type)) {
                entity.setOxidizing(CopperGolem.Oxidizing.waxed());
            } else if ("at_time".equals(type) && json.has("oxidizingTime")) {
                entity.setOxidizing(CopperGolem.Oxidizing.atTime(json.get("oxidizingTime").getAsLong()));
            } else {
                entity.setOxidizing(CopperGolem.Oxidizing.unset());
            }
        }
    }

    @Nonnull
    @Override
    public JsonObject saveData(@Nonnull CopperGolem entity) {
        JsonObject json = super.saveData(entity);
        json.addProperty("weathering", entity.getWeatheringState().name());
        json.addProperty("golemState", entity.getGolemState().name());

        CopperGolem.Oxidizing oxidizing = entity.getOxidizing();
        if (oxidizing == CopperGolem.Oxidizing.waxed()) {
            json.addProperty("oxidizingType", "waxed");
        } else if (oxidizing instanceof CopperGolem.Oxidizing.AtTime atTime) {
            json.addProperty("oxidizingType", "at_time");
            json.addProperty("oxidizingTime", atTime.time());
        } else {
            json.addProperty("oxidizingType", "unset");
        }
        return json;
    }

    private static String humanize(String input) {
        String[] words = input.toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder output = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!output.isEmpty()) output.append(' ');
            output.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return output.toString();
    }
}
