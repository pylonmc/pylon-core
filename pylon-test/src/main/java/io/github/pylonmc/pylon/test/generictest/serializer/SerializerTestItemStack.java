package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class SerializerTestItemStack implements GenericTest {
    @Override
    public void run() {
        ItemStack value = new ItemStack(Material.ACACIA_BOAT);
        // Just random properties to test
        value.editMeta((meta) -> {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setUnbreakable(true);
            meta.setCustomModelData(2);
            meta.setGlider(true);
        });

        SerializerTests.testSerializer(value, PylonSerializers.ITEM_STACK);
    }
}
