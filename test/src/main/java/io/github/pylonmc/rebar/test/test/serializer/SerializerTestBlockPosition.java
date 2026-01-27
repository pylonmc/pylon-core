package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.test.RebarTest;


public class SerializerTestBlockPosition extends SerializerTest<BlockPosition> {
    public SerializerTestBlockPosition() {
        super(new BlockPosition(RebarTest.testWorld, 5, 10, 185), RebarSerializers.BLOCK_POSITION);
    }
}
