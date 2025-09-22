package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.util.position.BlockPosition;

import java.util.UUID;


public class SerializerTestBlockPositionNoWorld extends SerializerTest<BlockPosition> {
    public SerializerTestBlockPositionNoWorld() {
        super(new BlockPosition(5, 10, 185), PylonSerializers.BLOCK_POSITION);
    }
}
