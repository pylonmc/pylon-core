package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;


public class SerializerTestBlockPositionNoWorld extends SerializerTest<BlockPosition> {
    public SerializerTestBlockPositionNoWorld() {
        super(new BlockPosition(null, 5, 10, 185), PylonSerializers.BLOCK_POSITION);
    }
}
