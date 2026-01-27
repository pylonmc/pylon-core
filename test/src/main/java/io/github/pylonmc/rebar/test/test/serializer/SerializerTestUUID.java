package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;

import java.util.UUID;


public class SerializerTestUUID extends SerializerTest<UUID> {
    public SerializerTestUUID() {
        super(UUID.randomUUID(), RebarSerializers.UUID);
    }
}
