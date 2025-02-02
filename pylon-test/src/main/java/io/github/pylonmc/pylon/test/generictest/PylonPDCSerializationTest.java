package io.github.pylonmc.pylon.test.generictest;

import io.github.pylonmc.pylon.core.persistence.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.NamespacedKey;

import static org.assertj.core.api.Assertions.assertThat;

public class PylonPDCSerializationTest implements GenericTest {
    @Override
    public void run() {
        NamespacedKey innerKey = new NamespacedKey(TestAddon.instance(), "somekey");
        var innerType = PylonSerializers.STRING;
        String innerValue = "iiroirimjrg";

        NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
        var type = PylonSerializers.TAG_CONTAINER;
        PylonPersistentDataContainer value = new PylonPersistentDataContainer();
        value.set(innerKey, innerType, innerValue);


        PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
        pdc.set(key, type, value);
        byte[] bytes = pdc.serializeToBytes();

        PylonPersistentDataContainer newPdc = new PylonPersistentDataContainer(bytes);

        assertThat(newPdc.get(key, type))
                .isNotNull()
                .extracting(p -> p.get(innerKey, innerType))
                .isNotNull()
                .isEqualTo(innerValue);
    }
}
