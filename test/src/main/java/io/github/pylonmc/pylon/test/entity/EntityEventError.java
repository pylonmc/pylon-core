package io.github.pylonmc.pylon.test.entity;

import io.github.pylonmc.pylon.core.entity.PylonEntity;
import io.github.pylonmc.pylon.core.entity.base.PylonInteractEntity;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;


public class EntityEventError extends PylonEntity<LivingEntity> implements PylonInteractEntity {

    public static final NamespacedKey KEY = PylonTest.key("entity_event_error");

    public EntityEventError(@NotNull Location location) {
        super(KEY, location.getWorld().spawn(location, Skeleton.class));
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public EntityEventError(@NotNull LivingEntity entity) {
        super(entity);
    }

    @Override @EventHandler
    public void onInteract(@NotNull PlayerInteractEntityEvent event, @NotNull EventPriority priority) {
        throw new RuntimeException("This exception is thrown as part of a test");
    }
}
