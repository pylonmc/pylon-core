package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import java.util.Set;

public class SerializerTestSetOfSetOfStrings extends SerializerTest {
    public SerializerTestSetOfSetOfStrings() {
        super(
                Set.of(Set.of("bruh", "bruuuu"), Set.of("*screaming*"), Set.of()),
                RebarSerializers.SET.setTypeFrom(RebarSerializers.SET.setTypeFrom(RebarSerializers.STRING))
        );
    }
}
