package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.persistence.pdc.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static org.assertj.core.api.Assertions.assertThat;

//  static <T> void testSerializer(T value, PersistentDataType<?, T> type)
public abstract class SerializerTest<T> extends SyncTest {
    private final T value;
    private final PersistentDataType<?, T> type;

    protected SerializerTest(T value, PersistentDataType<?, T> type) {
        super();
        this.value = value;
        this.type = type;
    }

    @Override
    public void test() {
        NamespacedKey key = new NamespacedKey(PylonTest.instance(), "key");

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
