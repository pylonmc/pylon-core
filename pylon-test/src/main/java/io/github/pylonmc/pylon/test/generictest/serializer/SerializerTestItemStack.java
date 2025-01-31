package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializerTestItemStack implements GenericTest {
    @Override
    public void run() {
        NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
        var type = PylonSerializers.ITEM_STACK;
        ItemStack value = new ItemStack(Material.ACACIA_BOAT);
        // Just random properties to test
        value.editMeta((meta) -> {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setUnbreakable(true);
            meta.setCustomModelData(2);
            meta.setGlider(true);
        });

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
