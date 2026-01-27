package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.PylonSerializers;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class SerializerTestItemStack extends SerializerTest<ItemStack> {
    private static @NotNull ItemStack getStack() {
        ItemStack value = new ItemStack(Material.ACACIA_BOAT);
        // Just random properties to test
        value.editMeta((meta) -> {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setUnbreakable(true);
            meta.setCustomModelData(2);
            meta.setGlider(true);
        });
        return value;
    }

    public SerializerTestItemStack() {
        super(getStack(), PylonSerializers.ITEM_STACK);
    }
}
