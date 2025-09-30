package io.github.pylonmc.pylon.test.item;

import io.github.pylonmc.pylon.core.item.PylonItem;
import io.github.pylonmc.pylon.core.item.base.PylonBrewingStandFuel;
import io.github.pylonmc.pylon.core.item.builder.PylonItemStackBuilder;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class OminousBlazePower extends PylonItem implements PylonBrewingStandFuel {

    public static final NamespacedKey KEY = PylonTest.key("ominous_blaze_powder");
    public static final ItemStack STACK = PylonItemStackBuilder.of(Material.DIAMOND_SWORD, KEY)
            .name("<ff0000>OMINOUS BLAZE POWDER")
            .lore("<#ff0000>VERY SCARY")
            .lore("<#222222>OH NO")
            .build();
    public static boolean handlerCalled;

    public OminousBlazePower(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onUsedAsBrewingStandFuel(@NotNull BrewingStandFuelEvent event) {
        event.setCancelled(true);
        handlerCalled = true;
    }
}