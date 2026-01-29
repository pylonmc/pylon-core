package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;

import java.util.Set;


public class SerializerTestSetOfInts extends SerializerTest {
    public SerializerTestSetOfInts() {
        super(Set.of(1, 2, 20, 40), RebarSerializers.SET.setTypeFrom(RebarSerializers.INTEGER));
    }
}
