package io.github.pylonmc.rebar.test.test.serializer;


import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.test.RebarTest;
import org.bukkit.NamespacedKey;


public class SerializerTestNamespacedKey extends SerializerTest<NamespacedKey> {
    public SerializerTestNamespacedKey() {
        super(RebarTest.key("some_key"), RebarSerializers.NAMESPACED_KEY);
    }
}
