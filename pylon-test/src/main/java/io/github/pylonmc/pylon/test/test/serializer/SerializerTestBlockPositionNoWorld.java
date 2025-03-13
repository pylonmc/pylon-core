package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.util.position.BlockPosition;


public class SerializerTestBlockPositionNoWorld extends SerializerTest<BlockPosition> {
    public SerializerTestBlockPositionNoWorld() {
        super(new BlockPosition(null, 5, 10, 185), PylonSerializers.BLOCK_POSITION);
    }
}
