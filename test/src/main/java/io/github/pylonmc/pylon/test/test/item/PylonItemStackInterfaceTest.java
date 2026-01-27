package io.github.pylonmc.pylon.test.test.item;

import io.github.pylonmc.rebar.gametest.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.item.OminousBlazePower;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.BrewingStandFuelEvent;


public class PylonItemStackInterfaceTest extends GameTest {

    public PylonItemStackInterfaceTest() {
        super(new GameTestConfig.Builder(PylonTest.key("pylon_item_stack_interface_test"))
                .size(0)
                .timeoutTicks(100)
                .setUp((test) -> {
                    OminousBlazePower.handlerCalled = false;

                    test.succeedWhen(() -> OminousBlazePower.handlerCalled);

                    Block block = test.getWorld().getBlockAt(test.location());
                    block.setType(Material.BREWING_STAND);
                    Bukkit.getPluginManager().callEvent(
                            new BrewingStandFuelEvent(block, OminousBlazePower.STACK, 1));

                })
                .build());
    }
}
