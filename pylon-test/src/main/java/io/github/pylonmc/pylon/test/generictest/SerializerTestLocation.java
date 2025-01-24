package io.github.pylonmc.pylon.test.generictest;

import io.github.pylonmc.pylon.core.persistence.PylonPersistentDataContainer;
import io.github.pylonmc.pylon.core.persistence.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializerTestLocation implements GenericTest {
    @Override
    public void run() {
        NamespacedKey key = new NamespacedKey(TestAddon.instance(), "key");
        var type = PylonSerializers.LOCATION;
        Location value = new Location(TestAddon.testWorld, 5.0, 320.3, 38904.43443);
        value.setPitch(3.0F);
        value.setYaw(1.04F);

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

    @Override
    public void cleanup() {}
}
