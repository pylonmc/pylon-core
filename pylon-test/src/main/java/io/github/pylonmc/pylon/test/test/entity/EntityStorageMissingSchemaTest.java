package io.github.pylonmc.pylon.test.test.entity;

import io.github.pylonmc.pylon.core.entity.EntityStorage;
import io.github.pylonmc.pylon.core.entity.PylonEntity;
import io.github.pylonmc.pylon.core.entity.PylonEntitySchema;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class EntityStorageMissingSchemaTest extends AsyncTest {

    public static class SimpleEntity extends PylonEntity<PylonEntitySchema, LivingEntity> {

        public SimpleEntity(@NotNull PylonEntitySchema schema, @NotNull Location location) {
            super(schema, location.getWorld().spawn(location, Skeleton.class));
        }

        @SuppressWarnings("unused")
        public SimpleEntity(@NotNull PylonEntitySchema schema, @NotNull LivingEntity entity) {
            super(schema, entity);
        }
    }

    public static final PylonEntitySchema SIMPLE_ENTITY = new PylonEntitySchema(
            PylonTest.key("entity_storage_missing_schema_test_entity"),
            LivingEntity.class,
            SimpleEntity.class
    );

    @Override
    protected void test() {
        SIMPLE_ENTITY.register();

        Chunk chunk = TestUtil.getRandomChunk(false).join();
        Location location = chunk.getBlock(5, 100, 5).getLocation();
        SimpleEntity pylonEntity = TestUtil.runSync(() -> new SimpleEntity(SIMPLE_ENTITY, location)).join();
        UUID uuid = pylonEntity.getEntity().getUniqueId();
        EntityStorage.add(pylonEntity);

        assertThat(EntityStorage.isPylonEntity(uuid))
                .isTrue();
        assertThat(EntityStorage.get(uuid))
                .isNotNull()
                .isInstanceOf(SimpleEntity.class);
        TestUtil.unloadChunk(chunk).join();
        TestUtil.waitUntil(() -> !chunk.isEntitiesLoaded()).join();

        PylonRegistry.ENTITIES.unregister(SIMPLE_ENTITY);

        TestUtil.loadChunk(chunk).join();
        TestUtil.waitUntil(chunk::isEntitiesLoaded).join();
        assertThat(EntityStorage.isPylonEntity(uuid))
                .isFalse();
        assertThat(EntityStorage.get(uuid))
                .isNull();
        TestUtil.unloadChunk(chunk).join();
        TestUtil.waitUntil(() -> !chunk.isEntitiesLoaded()).join();

        SIMPLE_ENTITY.register();

        TestUtil.loadChunk(chunk).join();
        TestUtil.waitUntil(chunk::isEntitiesLoaded).join();
        assertThat(EntityStorage.isPylonEntity(uuid))
                .isTrue();
        assertThat(EntityStorage.get(uuid))
                .isNotNull()
                .isInstanceOf(SimpleEntity.class);
    }
}
