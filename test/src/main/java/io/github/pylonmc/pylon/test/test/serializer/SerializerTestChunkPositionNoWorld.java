package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.util.position.ChunkPosition;


public class SerializerTestChunkPositionNoWorld extends SerializerTest<ChunkPosition> {
    public SerializerTestChunkPositionNoWorld() {
        super(new ChunkPosition(29, -19), PylonSerializers.CHUNK_POSITION);
    }
}
