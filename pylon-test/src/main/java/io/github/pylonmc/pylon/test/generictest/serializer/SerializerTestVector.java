package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import org.bukkit.util.Vector;

public class SerializerTestVector implements GenericTest {
    @Override
    public void run() {
        SerializerTests.testSerializer(
                new Vector(1.5, 2.2, 3.489),
                PylonSerializers.VECTOR
        );
    }
}
