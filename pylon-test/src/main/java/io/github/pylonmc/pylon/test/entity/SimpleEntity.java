package io.github.pylonmc.pylon.test.entity;

import io.github.pylonmc.pylon.core.entity.PylonEntity;
import io.github.pylonmc.pylon.core.entity.PylonEntitySchema;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;


public class SimpleEntity extends PylonEntity<PylonEntitySchema, LivingEntity> {

    private final NamespacedKey key = PylonTest.key("some_quantity");
    private final int someQuantity;

    public SimpleEntity(@NotNull PylonEntitySchema schema, @NotNull Location location) {
        super(schema, location.getWorld().spawn(location, Skeleton.class));
        getEntity().setHealth(999);
        someQuantity = 69;
    }

    protected SimpleEntity(@NotNull PylonEntitySchema schema, @NotNull Skeleton entity) {
        super(schema, entity);
        someQuantity = entity.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }

    @Override
    public void write() {
        getEntity().getPersistentDataContainer().set(key, PersistentDataType.INTEGER, someQuantity);
    }

    public int getSomeQuantity() {
        return someQuantity;
    }
}
