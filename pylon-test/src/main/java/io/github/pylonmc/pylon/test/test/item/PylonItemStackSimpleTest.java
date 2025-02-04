package io.github.pylonmc.pylon.test.test.item;

import io.github.pylonmc.pylon.core.item.ItemStackBuilder;
import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.item.SimplePylonItem;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

public class PylonItemStackSimpleTest extends SyncTest {
    @Override
    public void test() {
        new PylonItemSchema(
                PylonTest.key("pylon_item_stack_simple_test"),
                SimplePylonItem.class,
                new ItemStackBuilder(Material.DIAMOND_SWORD)
                        .set(DataComponentTypes.CUSTOM_NAME, Component.text("Ominous blaze powder")
                                .color(TextColor.color(255, 0, 0)))
                        .set(DataComponentTypes.LORE, ItemLore.lore()
                                .addLine(Component.text("VERY SCARY", TextColor.color(200, 0, 0)))
                                .addLine(Component.text("OH NO", TextColor.color(60, 60, 60)))
                                .build())
                        .build()
        ).register();
    }
}
