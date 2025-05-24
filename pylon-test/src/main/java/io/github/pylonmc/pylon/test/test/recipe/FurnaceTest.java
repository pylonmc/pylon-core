package io.github.pylonmc.pylon.test.test.recipe;

import io.github.pylonmc.pylon.core.recipe.RecipeTypes;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.item.Items;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class FurnaceTest extends GameTest {

    public FurnaceTest() {
        super(new GameTestConfig.Builder(PylonTest.key("furnace_test"))
                .size(0)
                .setUp(test -> {
                    ItemStack stickyStick = Items.STICKY_STICK_ITEM_STACK;
                    ItemStack diamond = new ItemStack(Material.DIAMOND);
                    RecipeTypes.VANILLA_FURNACE.addRecipe(new FurnaceRecipe(
                            PylonTest.key("sticky_stick_furnace"),
                            diamond,
                            new RecipeChoice.ExactChoice(stickyStick),
                            0.1f,
                            10
                    ));

                    Block furnace = test.position().getBlock();
                    furnace.setType(Material.FURNACE);
                    Furnace state = (Furnace) furnace.getState();
                    FurnaceInventory inventory = state.getInventory();
                    inventory.setFuel(new ItemStack(Material.STICK));
                    inventory.setSmelting(stickyStick);

                    test.succeedWhen(() -> {
                        FurnaceInventory inv = ((Furnace) furnace.getState()).getInventory();
                        ItemStack result = inv.getResult();
                        return diamond.equals(result);
                    });
                })
                .build()
        );
    }
}
