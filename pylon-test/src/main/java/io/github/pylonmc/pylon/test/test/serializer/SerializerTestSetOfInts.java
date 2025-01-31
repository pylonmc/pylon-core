package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import java.util.Set;


public class SerializerTestSetOfInts extends SerializerTest {
    public SerializerTestSetOfInts() {
        super(Set.of(1, 2, 20, 40), PylonSerializers.SET.setTypeFrom(PylonSerializers.INTEGER));
    }
}
