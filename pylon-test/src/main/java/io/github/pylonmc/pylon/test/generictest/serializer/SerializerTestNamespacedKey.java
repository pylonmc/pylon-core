package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.NamespacedKey;

public class SerializerTestNamespacedKey implements GenericTest {
    @Override
    public void run() {
        SerializerTests.testSerializer(
                new NamespacedKey(TestAddon.instance(), "value"),
                PylonSerializers.NAMESPACED_KEY
        );
    }
}
