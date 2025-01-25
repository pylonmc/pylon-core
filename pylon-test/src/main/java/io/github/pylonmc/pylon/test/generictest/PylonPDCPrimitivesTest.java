package io.github.pylonmc.pylon.test.generictest;

import io.github.pylonmc.pylon.core.persistence.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.core.persistence.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.NamespacedKey;

import static org.assertj.core.api.Assertions.assertThat;

public class PylonPDCPrimitivesTest implements GenericTest {
    @Override
    public void run() {
        // Byte
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.BYTE;
            byte value = 7;

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Short
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.SHORT;
            short value = 32;

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Integer
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.INTEGER;
            int value = 73823;

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Long
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.LONG;
            long value = 9;

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Float
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.FLOAT;
            float value = 3.59835F;

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Double
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.DOUBLE;
            double value = 2.329;

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Boolean
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.BOOLEAN;
            boolean value = true;

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // String
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.STRING;
            String value = "bruh";

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Byte array
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.BYTE_ARRAY;
            byte[] value = {1, 40, 110, 3};

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Integer array
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.INTEGER_ARRAY;
            int[] value = {20293, 8948, -290290};

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Long array
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.LONG_ARRAY;
            long[] value = {498748947, 493884, -39};

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isEqualTo(value);
        }

        // Tag container
        {
            NamespacedKey innerKey = new NamespacedKey(TestAddon.instance(), "somekey");
            var innerType = PylonSerializers.STRING;
            String innerValue = "iiroirimjrg";

            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.TAG_CONTAINER;
            PylonPersistentDataContainer value = new PylonPersistentDataContainer();
            value.set(innerKey, innerType, innerValue);

            PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
            pdc.set(key, type, value);
            assertThat(pdc.get(key, type))
                    .isNotNull()
                    .extracting(p -> p.get(innerKey, innerType))
                    .isNotNull()
                    .isEqualTo(innerValue);

        }
    }
}
