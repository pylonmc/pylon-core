package io.github.pylonmc.pylon.test.generictest;

import io.github.pylonmc.pylon.core.item.ItemStackBuilder;
import io.github.pylonmc.pylon.core.item.PylonItem;
import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.item.SimplePylonItem;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

public class PylonItemStackSimpleTest implements GenericTest {
    @Override
    public void run() {
        new PylonItemSchema(
                TestAddon.key("pylon_item_stack_simple_test"),
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
