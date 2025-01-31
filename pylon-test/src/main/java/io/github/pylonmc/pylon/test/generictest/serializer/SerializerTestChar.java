package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;

public class SerializerTestChar implements GenericTest {
    @Override
    public void run() {
        SerializerTests.testSerializer('s', PylonSerializers.CHAR);
    }
}
