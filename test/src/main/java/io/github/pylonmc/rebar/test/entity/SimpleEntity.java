package io.github.pylonmc.rebar.test.entity;

import io.github.pylonmc.rebar.entity.RebarEntity;
import io.github.pylonmc.rebar.test.RebarTest;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;


public class SimpleEntity extends RebarEntity<LivingEntity> {

    public static final NamespacedKey KEY = RebarTest.key("simple_entity");
    private static final NamespacedKey QUANTITY_KEY = RebarTest.key("some_quantity");
    private final int someQuantity;

    public SimpleEntity(@NotNull Location location) {
        super(KEY, location.getWorld().spawn(location, Skeleton.class));
        getEntity().setAI(false);
        someQuantity = 69;
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public SimpleEntity(@NotNull LivingEntity entity) {
        super(entity);
        someQuantity = entity.getPersistentDataContainer().get(QUANTITY_KEY, PersistentDataType.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(QUANTITY_KEY, PersistentDataType.INTEGER, someQuantity);
    }

    public int getSomeQuantity() {
        return someQuantity;
    }
}
