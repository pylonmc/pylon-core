package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.util.ChunkPosition;


public class SerializerTestChunkPositionNoWorld extends SerializerTest<ChunkPosition> {
    public SerializerTestChunkPositionNoWorld() {
        super(new ChunkPosition(null, 29, -19), PylonSerializers.CHUNK_POSITION);
    }
}
