package io.github.pylonmc.pylon.test.test.recipe;

import io.github.pylonmc.pylon.core.item.ItemStackBuilder;
import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.item.SimplePylonItem;
import io.github.pylonmc.pylon.test.PylonTest;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

@SuppressWarnings("UnstableApiUsage")
class StickyStick extends PylonItemSchema {
    public StickyStick() {
        super(
                PylonTest.key("sticky_stick"),
                SimplePylonItem.class,
                new ItemStackBuilder(Material.STICK)
                        .set(DataComponentTypes.ITEM_NAME, Component.text("Sticky Stick"))
                        .build()
        );
    }

    static StickyStick INSTANCE = new StickyStick();

    static {
        INSTANCE.register();
    }
}
