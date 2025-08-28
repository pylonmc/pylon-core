package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.util.position.ChunkPosition;
import io.github.pylonmc.pylon.test.PylonTest;


public class SerializerTestChunkPosition extends SerializerTest<ChunkPosition> {
    public SerializerTestChunkPosition() {
        super(new ChunkPosition(PylonTest.testWorld, 29, -19), PylonSerializers.CHUNK_POSITION);
    }
}
