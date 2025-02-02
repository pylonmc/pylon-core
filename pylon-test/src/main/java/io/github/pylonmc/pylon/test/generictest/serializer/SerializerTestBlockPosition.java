package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;

public class SerializerTestBlockPosition implements GenericTest {
    @Override
    public void run() {
        // With world
        SerializerTests.testSerializer(
                new BlockPosition(TestAddon.testWorld, 5, 10, 185),
                PylonSerializers.BLOCK_POSITION
        );

        // Without world
        SerializerTests.testSerializer(
                new BlockPosition(null, 5, 10, 185),
                PylonSerializers.BLOCK_POSITION
        );
    }
}
