package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;

import java.util.UUID;


public class SerializerTestUUID extends SerializerTest<UUID> {
    public SerializerTestUUID() {
        super(UUID.randomUUID(), PylonSerializers.UUID);
    }
}
