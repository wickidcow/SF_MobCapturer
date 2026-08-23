package net.guizhanss.minecraft.guizhanlib.gugu.minecraft.helpers.entity;

import org.bukkit.entity.Villager.Profession;

public final class VillagerHelper {
    private VillagerHelper() {}
    public static String getProfessionName(Profession profession) {
        return profession == null ? "Unknown" : DisplayNameHelper.humanize(profession.toString());
    }
}
