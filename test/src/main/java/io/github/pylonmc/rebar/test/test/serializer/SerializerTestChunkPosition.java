package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import io.github.pylonmc.rebar.test.RebarTest;


public class SerializerTestChunkPosition extends SerializerTest<ChunkPosition> {
    public SerializerTestChunkPosition() {
        super(new ChunkPosition(RebarTest.testWorld, 29, -19), RebarSerializers.CHUNK_POSITION);
    }
}
