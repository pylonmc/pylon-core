package io.github.pylonmc.pylon.test.generictest;

import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.persistence.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializerTestBlockPosition implements GenericTest {
    @Override
    public void run() {
        // With world
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.BLOCK_POSITION;
            BlockPosition value = new BlockPosition(TestAddon.testWorld, 5, 10, 185);

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

        // Without world
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.BLOCK_POSITION;
            BlockPosition value = new BlockPosition(null, 5, 10, 185);

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
}
