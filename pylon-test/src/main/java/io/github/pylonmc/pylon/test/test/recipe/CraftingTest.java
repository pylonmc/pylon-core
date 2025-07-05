package io.github.pylonmc.pylon.test.test.recipe;

import io.github.pylonmc.pylon.core.recipe.RecipeType;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import io.github.pylonmc.pylon.test.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CraftingTest extends SyncTest {

    @Override
    protected void test() {
        ItemStack stickyStick = Items.STICKY_STICK_STACK;
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemStack nothing = new ItemStack(Material.AIR);
        ItemStack normalStick = new ItemStack(Material.STICK);

        // Shaped
        {
            RecipeType.VANILLA_SHAPED.addRecipe(
                    new ShapedRecipe(PylonTest.key("sticky_stick_shaped"), diamond)
                            .shape(
                                    " s ",
                                    "sSs",
                                    " s "
                            )
                            .setIngredient('s', Material.STICK)
                            .setIngredient('S', stickyStick)
            );
            ItemStack[] crafting = {
                    nothing, normalStick, nothing,
                    normalStick, stickyStick, normalStick,
                    nothing, normalStick, nothing
            };
            assertThat(Bukkit.craftItem(crafting, PylonTest.testWorld))
                    .isEqualTo(diamond);
        }

        // Shapeless
        {
            RecipeType.VANILLA_SHAPELESS.addRecipe(
                    new ShapelessRecipe(PylonTest.key("sticky_stick_shapeless"), normalStick)
                            .addIngredient(Material.DIAMOND)
                            .addIngredient(stickyStick)
            );
            ItemStack[] crafting = new ItemStack[9];
            Arrays.fill(crafting, nothing);
            crafting[0] = stickyStick;
            crafting[1] = diamond;
            assertThat(Bukkit.craftItem(crafting, PylonTest.testWorld))
                    .isEqualTo(normalStick);
        }

        // With custom output
        {
            RecipeType.VANILLA_SHAPED.addRecipe(
                    new ShapedRecipe(PylonTest.key("sticky_stick_shaped_custom_output"), stickyStick)
                            .shape(
                                    " s ",
                                    "sDs",
                                    " s "
                            )
                            .setIngredient('s', Material.STICK)
                            .setIngredient('D', diamond)
            );
            ItemStack[] crafting = {
                    nothing, normalStick, nothing,
                    normalStick, diamond, normalStick,
                    nothing, normalStick, nothing
            };
            assertThat(Bukkit.craftItem(crafting, PylonTest.testWorld))
                    .isEqualTo(stickyStick);
        }
    }
}
