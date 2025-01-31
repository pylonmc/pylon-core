package io.github.pylonmc.pylon.test.generictest;

import io.github.pylonmc.pylon.core.persistence.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializerTestSet implements GenericTest {
    @Override
    public void run() {
        // Set of integers
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.SET.setTypeFrom(PylonSerializers.INTEGER);
            Set<Integer> value = Set.of(1, 2, 20, 40);

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

        // Set of set of strings
        {
            NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
            var type = PylonSerializers.SET.setTypeFrom(
                    PylonSerializers.SET.setTypeFrom(PylonSerializers.STRING));
            Set<Set<String>> value = Set.of(Set.of("bruh", "bruuuu"), Set.of("*screaming*"), Set.of());

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
