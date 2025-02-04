package io.github.pylonmc.pylon.test.test.serializer;


import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;


public class SerializerTestNamespacedKey extends SerializerTest<NamespacedKey> {
    public SerializerTestNamespacedKey() {
        super(PylonTest.key("some_key"), PylonSerializers.NAMESPACED_KEY);
    }
}
