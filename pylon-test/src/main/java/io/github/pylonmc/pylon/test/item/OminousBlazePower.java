package io.github.pylonmc.pylon.test.item;

import io.github.pylonmc.pylon.core.item.PylonItem;
import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.item.base.BrewingStandFuel;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class OminousBlazePower extends PylonItem<PylonItemSchema> implements BrewingStandFuel {
    public static boolean handlerCalled;

    public OminousBlazePower(@NotNull PylonItemSchema schema, @NotNull ItemStack stack) {
        super(schema, stack);
    }

    @Override
    public void onUsedAsBrewingStandFuel(@NotNull BrewingStandFuelEvent event) {
        event.setCancelled(true);
        handlerCalled = true;
    }
}