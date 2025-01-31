package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static org.assertj.core.api.Assertions.assertThat;

class SerializerTests {

    private SerializerTests() {
    }

    static <T> void testSerializer(T value, PersistentDataType<?, T> type) {
        NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");

        ItemStack stack = new ItemStack(Material.ACACIA_BOAT);
        stack.editMeta(meta -> meta.getPersistentDataContainer()
                .set(key, type, value));
        assertThat(stack.getPersistentDataContainer().get(key, type))
                .isEqualTo(value);

        PylonPersistentDataContainer pdc = new PylonPersistentDataContainer();
        pdc.set(key, type, value);
        assertThat(pdc.get(key, type))
                .isEqualTo(value);
    }
}
