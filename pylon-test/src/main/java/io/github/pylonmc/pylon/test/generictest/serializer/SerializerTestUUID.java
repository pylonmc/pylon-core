package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;

import java.util.UUID;

public class SerializerTestUUID implements GenericTest {
    @Override
    public void run() {
        SerializerTests.testSerializer(UUID.randomUUID(), PylonSerializers.UUID);
    }
}
