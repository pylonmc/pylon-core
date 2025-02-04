package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.block.ChunkPosition;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;


public class SerializerTestChunkPositionNoWorld extends SerializerTest<ChunkPosition> {
    public SerializerTestChunkPositionNoWorld() {
        super(new ChunkPosition(null, 29, -19), PylonSerializers.CHUNK_POSITION);
    }
}
