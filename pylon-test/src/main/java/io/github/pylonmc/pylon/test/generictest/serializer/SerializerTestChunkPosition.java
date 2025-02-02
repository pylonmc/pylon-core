package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.block.ChunkPosition;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;

public class SerializerTestChunkPosition implements GenericTest {
    @Override
    public void run() {
        // With world
        SerializerTests.testSerializer(
                new ChunkPosition(TestAddon.testWorld, 7, 85),
                PylonSerializers.CHUNK_POSITION
        );

        // Without world
        SerializerTests.testSerializer(
                new ChunkPosition(null, 7, 85),
                PylonSerializers.CHUNK_POSITION
        );
    }
}
