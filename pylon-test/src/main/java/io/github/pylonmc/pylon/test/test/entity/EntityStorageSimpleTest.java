package io.github.pylonmc.pylon.test.test.entity;

import io.github.pylonmc.pylon.core.entity.EntityStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.entity.SimpleEntity;
import org.bukkit.NamespacedKey;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class EntityStorageSimpleTest extends GameTest {

    public EntityStorageSimpleTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "entity_storage_add_test"))
                .size(1)
                .setUp((test) -> {
                    SimpleEntity pylonEntity = new SimpleEntity(test.location());
                    UUID uuid = pylonEntity.getEntity().getUniqueId();
                    EntityStorage.add(pylonEntity);

                    assertThat(EntityStorage.isPylonEntity(uuid))
                            .isTrue();
                    assertThat(EntityStorage.get(uuid))
                            .isNotNull()
                            .isInstanceOf(SimpleEntity.class);
                    assertThat(EntityStorage.getAs(SimpleEntity.class, uuid))
                            .isNotNull()
                            .extracting(SimpleEntity::getSomeQuantity)
                            .isEqualTo(69);
                    assertThat(pylonEntity.getEntity().hasAI())
                            .isFalse();

                    pylonEntity.getEntity().remove();

                    assertThat(EntityStorage.isPylonEntity(uuid))
                            .isFalse();
                    assertThat(EntityStorage.get(uuid))
                            .isNull();

                })
                .build());
    }
}
