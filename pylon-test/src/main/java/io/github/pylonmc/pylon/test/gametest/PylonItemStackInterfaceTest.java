package io.github.pylonmc.pylon.test.gametest;

import io.github.pylonmc.pylon.core.item.ItemStackBuilder;
import io.github.pylonmc.pylon.core.item.PylonItem;
import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.item.base.BrewingStandFuel;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.TestAddon;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PylonItemStackInterfaceTest {
    private static boolean handlerCalled;

    public static class OminousBlazePower extends PylonItem<PylonItemSchema> implements BrewingStandFuel {
        public OminousBlazePower(@NotNull PylonItemSchema schema, @NotNull ItemStack stack) {
            super(schema, stack);
        }

        @Override
        public void onUsedAsBrewingStandFuel(@NotNull BrewingStandFuelEvent event) {
            event.setCancelled(true);
            handlerCalled = true;
        }
    }

    public static @NotNull GameTestConfig get() {
        return new GameTestConfig.Builder(TestAddon.key("pylon_item_stack_interface_test"))
                .size(0)
                .timeoutTicks(100)
                .setUp((test) -> {
                    PylonItemSchema ominousBlazePowder = new PylonItemSchema(
                            TestAddon.key("pylon_item_stack_interface_test"),
                            OminousBlazePower.class,
                            new ItemStackBuilder(Material.DIAMOND_SWORD)
                                    .set(DataComponentTypes.CUSTOM_NAME, Component.text("Ominous blaze powder")
                                            .color(TextColor.color(255, 0, 0)))
                                    .set(DataComponentTypes.LORE, ItemLore.lore()
                                            .addLine(Component.text("VERY SCARY", TextColor.color(200, 0, 0)))
                                            .addLine(Component.text("OH NO", TextColor.color(60, 60, 60)))
                                            .build())
                                    .build()
                    ).register();

                    test.succeedWhen(() -> handlerCalled);

                    Block block = test.getWorld().getBlockAt(test.location());
                    block.setType(Material.BREWING_STAND);
                    Bukkit.getPluginManager().callEvent(
                            new BrewingStandFuelEvent(block, ominousBlazePowder.getStack(), 1));

                })
                .build();
    }
}
