package io.github.pylonmc.rebar.test.test.entity;

import io.github.pylonmc.rebar.entity.EntityStorage;
import io.github.pylonmc.rebar.gametest.GameTestConfig;
import io.github.pylonmc.rebar.test.RebarTest;
import io.github.pylonmc.rebar.test.base.GameTest;
import io.github.pylonmc.rebar.test.entity.SimpleEntity;
import org.bukkit.NamespacedKey;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class EntityStorageSimpleTest extends GameTest {

    public EntityStorageSimpleTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(RebarTest.instance(), "entity_storage_add_test"))
                .size(1)
                .setUp((test) -> {
                    SimpleEntity rebarEntity = new SimpleEntity(test.location());
                    UUID uuid = rebarEntity.getEntity().getUniqueId();
                    EntityStorage.add(rebarEntity);

                    assertThat(EntityStorage.isRebarEntity(uuid))
                            .isTrue();
                    assertThat(EntityStorage.get(uuid))
                            .isNotNull()
                            .isInstanceOf(SimpleEntity.class);
                    assertThat(EntityStorage.getAs(SimpleEntity.class, uuid))
                            .isNotNull()
                            .extracting(SimpleEntity::getSomeQuantity)
                            .isEqualTo(69);
                    assertThat(rebarEntity.getEntity().hasAI())
                            .isFalse();

                    rebarEntity.getEntity().remove();

                    assertThat(EntityStorage.isRebarEntity(uuid))
                            .isFalse();
                    assertThat(EntityStorage.get(uuid))
                            .isNull();

                })
                .build());
    }
}
