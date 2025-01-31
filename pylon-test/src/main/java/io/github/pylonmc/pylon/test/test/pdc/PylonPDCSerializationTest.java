package io.github.pylonmc.pylon.test.test.pdc;

import io.github.pylonmc.pylon.core.persistence.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import org.bukkit.NamespacedKey;

import static org.assertj.core.api.Assertions.assertThat;

public class PylonPDCSerializationTest extends SyncTest {
    @Override
    public void test() {
        NamespacedKey innerKey = new NamespacedKey(PylonTest.instance(), "somekey");
        var innerType = PylonSerializers.STRING;
        String innerValue = "iiroirimjrg";

        NamespacedKey key = new NamespacedKey(PylonTest.instance(), "key");
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
