package io.github.thebusybiscuit.mobcapturer.adapters.mobs;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.entity.AbstractNautilus;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.mobcapturer.adapters.InventoryAdapter;

class AbstractNautilusAdapter<T extends AbstractNautilus> extends AbstractTameableAdapter<T> implements InventoryAdapter<T> {

    public AbstractNautilusAdapter(@Nonnull Class<T> entityClass) {
        super(entityClass);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void applyInventory(T entity, Map<String, ItemStack> inventory) {
        entity.getInventory().setSaddle(inventory.get("saddle"));
        entity.getInventory().setArmor(inventory.get("armor"));
    }

    @Nonnull
    @Override
    public Map<String, ItemStack> saveInventory(@Nonnull T entity) {
        Map<String, ItemStack> inventory = new HashMap<>();

        inventory.put("saddle", entity.getInventory().getSaddle());
        inventory.put("armor", entity.getInventory().getArmor());

        return inventory;
    }

}
