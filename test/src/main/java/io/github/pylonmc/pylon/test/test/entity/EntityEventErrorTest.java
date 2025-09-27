package io.github.pylonmc.pylon.test.test.entity;

import io.github.pylonmc.pylon.core.config.PylonConfig;
import io.github.pylonmc.pylon.core.entity.EntityStorage;
import io.github.pylonmc.pylon.core.gametest.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.entity.EntityEventError;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.UUID;

public class EntityEventErrorTest extends GameTest {
    public EntityEventErrorTest(){
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "entity_event_error_test"))
                .size(1)
                .setUp(test -> {
                    EntityEventError entity = new EntityEventError(test.location());
                    EntityStorage.add(entity);
                    UUID entityUUID = entity.getUuid();
                    for(int i = 0; i < PylonConfig.getAllowedEntityErrors() + 1; i++){
                        // Yes, this is cursed, yes it works.
                        new PlayerInteractEntityEvent(null, entity.getEntity()).callEvent();
                    }
                    test.succeedWhen(() -> !EntityStorage.isPylonEntity(entityUUID));
                })
                .build());
    }
}
