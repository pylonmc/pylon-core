package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.util.position.BlockPosition;
import io.github.pylonmc.pylon.test.PylonTest;


public class SerializerTestBlockPosition extends SerializerTest<BlockPosition> {
    public SerializerTestBlockPosition() {
        super(new BlockPosition(PylonTest.testWorld, 5, 10, 185), PylonSerializers.BLOCK_POSITION);
    }
}
