package io.github.pylonmc.pylon.test.item;

import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.item.SimplePylonItem;
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder;
import io.github.pylonmc.pylon.test.PylonTest;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;


public final class Items {

    private Items() {}

    public static final PylonItemSchema STICKY_STICK = new PylonItemSchema(
            PylonTest.key("sticky_stick"),
            SimplePylonItem.class,
            ItemStackBuilder.of(Material.STICK)
                    .set(DataComponentTypes.ITEM_NAME, Component.text("Sticky Stick"))
                    .build()
    );

    public static final PylonItemSchema OMINOUS_BLAZE_POWDER = new PylonItemSchema(
            PylonTest.key("ominous_blaze_powder"),
            OminousBlazePower.class,
            ItemStackBuilder.of(Material.DIAMOND_SWORD)
                    .name("<ff0000>OMINOUS BLAZE POWDER")
                    .lore("<#ff0000>VERY SCARY")
                    .lore("<#222222>OH NO")
                    .build()
    );

    public static void register() {
        STICKY_STICK.register();
        OMINOUS_BLAZE_POWDER.register();
    }
}
