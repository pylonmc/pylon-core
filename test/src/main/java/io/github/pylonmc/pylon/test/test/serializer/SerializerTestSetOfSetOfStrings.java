package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.PylonSerializers;
import java.util.Set;

public class SerializerTestSetOfSetOfStrings extends SerializerTest {
    public SerializerTestSetOfSetOfStrings() {
        super(
                Set.of(Set.of("bruh", "bruuuu"), Set.of("*screaming*"), Set.of()),
                PylonSerializers.SET.setTypeFrom(PylonSerializers.SET.setTypeFrom(PylonSerializers.STRING))
        );
    }
}
