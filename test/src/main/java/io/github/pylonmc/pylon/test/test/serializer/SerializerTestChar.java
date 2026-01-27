package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.PylonSerializers;


public class SerializerTestChar extends SerializerTest<Character> {
    public SerializerTestChar() {
        super('s', PylonSerializers.CHAR);
    }
}
