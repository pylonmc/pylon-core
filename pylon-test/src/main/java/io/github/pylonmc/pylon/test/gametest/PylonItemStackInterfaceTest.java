package io.github.pylonmc.pylon.test.gametest;

import io.github.pylonmc.pylon.core.item.ItemStackBuilder;
import io.github.pylonmc.pylon.core.item.PylonItem;
import io.github.pylonmc.pylon.core.item.interfaces.BrewingStandFuel;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.TestAddon;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PylonItemStackInterfaceTest {
    private static boolean handlerCalled = false;

    public static class OminousBlazePower extends PylonItem implements BrewingStandFuel {
        public OminousBlazePower(@NotNull NamespacedKey id, @NotNull ItemStack stack) {
            super(id, stack);
        }

        public OminousBlazePower(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public void onUsedAsBrewingStandFuel(@NotNull BrewingStandFuelEvent event) {
            event.setCancelled(true);
            handlerCalled = true;
        }
    }

    public static @NotNull GameTestConfig get() {
        return new GameTestConfig.Builder(TestAddon.key("pylon_item_stack_interface_test"))
                .size(1)
                .timeoutTicks(100)
                .setUp((test) -> {
                    OminousBlazePower ominousBlazePowder = new OminousBlazePower(
                            TestAddon.key("pylon_item_stack_interface_test"),
                            new ItemStackBuilder(Material.DIAMOND_SWORD)
                                    .set(DataComponentTypes.CUSTOM_NAME, Component.text("Ominous blaze powder")
                                            .color(TextColor.color(255, 0, 0)))
                                    .set(DataComponentTypes.LORE, ItemLore.lore()
                                            .addLine(Component.text("VERY SCARY", TextColor.color(200, 0, 0)))
                                            .addLine(Component.text("OH NO", TextColor.color(60, 60, 60)))
                                            .build())
                                    .build()
                    );

                    ominousBlazePowder.register();

                    test.succeedWhen(() -> handlerCalled);

                    Block block = test.getWorld().getBlockAt(test.location());
                    block.setType(Material.BREWING_STAND);
                    Bukkit.getPluginManager().callEvent(
                            new BrewingStandFuelEvent(block, ominousBlazePowder, 1));

                })
                .build();
    }
}
