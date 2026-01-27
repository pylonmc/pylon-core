package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.util.position.ChunkPosition;


public class SerializerTestChunkPositionNoWorld extends SerializerTest<ChunkPosition> {
    public SerializerTestChunkPositionNoWorld() {
        super(new ChunkPosition(29, -19), RebarSerializers.CHUNK_POSITION);
    }
}
