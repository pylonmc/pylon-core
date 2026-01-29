package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.util.position.BlockPosition;


public class SerializerTestBlockPositionNoWorld extends SerializerTest<BlockPosition> {
    public SerializerTestBlockPositionNoWorld() {
        super(new BlockPosition(5, 10, 185), RebarSerializers.BLOCK_POSITION);
    }
}
