package io.github.pylonmc.pylon.test.item;

import io.github.pylonmc.pylon.core.item.PylonItem;
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder;
import io.github.pylonmc.pylon.test.PylonTest;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;


public final class Items {

    private Items() {}

    public static final NamespacedKey STICKY_STICK_KEY = PylonTest.key("sticky_stick");
    public static final ItemStack STICKY_STICK_ITEM_STACK = ItemStackBuilder.defaultBuilder(Material.STICK, STICKY_STICK_KEY)
            .set(DataComponentTypes.ITEM_NAME, Component.text("Sticky Stick"))
            .build();

    public static void register() {
        PylonItem.register(PylonItem.class, STICKY_STICK_ITEM_STACK);
        PylonItem.register(OminousBlazePower.class, OminousBlazePower.ITEM_STACK);
    }
}
