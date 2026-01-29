package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;


public class SerializerTestChar extends SerializerTest<Character> {
    public SerializerTestChar() {
        super('s', RebarSerializers.CHAR);
    }
}
