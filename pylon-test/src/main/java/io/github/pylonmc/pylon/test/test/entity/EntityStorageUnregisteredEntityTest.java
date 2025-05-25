package io.github.pylonmc.pylon.test.test.entity;

import io.github.pylonmc.pylon.core.entity.EntityStorage;
import io.github.pylonmc.pylon.core.entity.PylonEntity;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class EntityStorageUnregisteredEntityTest extends AsyncTest {

    public static class UnregisteredEntity extends PylonEntity<LivingEntity> {

        public static final NamespacedKey KEY = PylonTest.key("unregistered_entity");

        public UnregisteredEntity(@NotNull Location location) {
            super(KEY, location.getWorld().spawn(location, Skeleton.class));
        }

        @SuppressWarnings("unused")
        public UnregisteredEntity(@NotNull LivingEntity entity, @NotNull PersistentDataContainer pdc) {
            super(KEY, entity);
        }
    }

    @Override
    protected void test() {
        PylonEntity.register(UnregisteredEntity.KEY, LivingEntity.class, UnregisteredEntity.class);

        Chunk chunk = TestUtil.getRandomChunk(false).join();
        Location location = chunk.getBlock(5, 100, 5).getLocation();
        UnregisteredEntity pylonEntity = TestUtil.runSync(() -> new UnregisteredEntity(location)).join();
        UUID uuid = pylonEntity.getEntity().getUniqueId();
        EntityStorage.add(pylonEntity);

        assertThat(EntityStorage.isPylonEntity(uuid))
                .isTrue();
        assertThat(EntityStorage.get(uuid))
                .isNotNull()
                .isInstanceOf(UnregisteredEntity.class);
        TestUtil.unloadChunk(chunk).join();
        TestUtil.waitUntil(() -> !chunk.isEntitiesLoaded()).join();

        PylonRegistry.ENTITIES.unregister(UnregisteredEntity.KEY);

        TestUtil.loadChunk(chunk).join();
        TestUtil.waitUntil(chunk::isEntitiesLoaded).join();
        assertThat(EntityStorage.isPylonEntity(uuid))
                .isFalse();
        assertThat(EntityStorage.get(uuid))
                .isNull();
        TestUtil.unloadChunk(chunk).join();
        TestUtil.waitUntil(() -> !chunk.isEntitiesLoaded()).join();

        PylonEntity.register(UnregisteredEntity.KEY, LivingEntity.class, UnregisteredEntity.class);

        TestUtil.loadChunk(chunk).join();
        TestUtil.waitUntil(chunk::isEntitiesLoaded).join();
        assertThat(EntityStorage.isPylonEntity(uuid))
                .isTrue();
        assertThat(EntityStorage.get(uuid))
                .isNotNull()
                .isInstanceOf(UnregisteredEntity.class);
    }
}
