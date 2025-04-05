package io.github.pylonmc.pylon.test.test.entity;

import io.github.pylonmc.pylon.core.entity.EntityStorage;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.entity.Entities;
import io.github.pylonmc.pylon.test.entity.SimpleEntity;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class EntityStorageChunkReloadTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        Location location = chunk.getBlock(5, 100, 5).getLocation();
        SimpleEntity pylonEntity = TestUtil.runSync(() -> new SimpleEntity(Entities.SIMPLE_ENTITY, location)).join();
        UUID uuid = pylonEntity.getEntity().getUniqueId();
        EntityStorage.add(pylonEntity);

        assertThat(EntityStorage.isPylonEntity(uuid))
                .isTrue();

        TestUtil.unloadChunk(chunk).join();
        TestUtil.waitUntil(() -> !chunk.isEntitiesLoaded()).join();

        assertThat(EntityStorage.isPylonEntity(uuid))
                .isFalse();

        TestUtil.loadChunk(chunk).join();
        TestUtil.waitUntil(chunk::isEntitiesLoaded).join();

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
    }
}
