package io.github.pylonmc.pylon.test.test.recipe;

import io.github.pylonmc.pylon.core.item.ItemStackBuilder;
import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.item.SimplePylonItem;
import io.github.pylonmc.pylon.core.recipe.RecipeType;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("UnstableApiUsage")
public class CraftingTest extends SyncTest {

    private static class StickyStick extends PylonItemSchema {
        public StickyStick() {
            super(
                    PylonTest.key("sticky_stick"),
                    SimplePylonItem.class,
                    new ItemStackBuilder(Material.STICK)
                            .set(DataComponentTypes.ITEM_NAME, Component.text("Sticky Stick"))
                            .build()
            );
        }
    }

    @Override
    protected void test() {
        StickyStick stickyStick = new StickyStick();
        stickyStick.register();
        ItemStack stickyStickStick = stickyStick.getStack();
        ItemStack result = new ItemStack(Material.DIAMOND);
        ItemStack nothing = new ItemStack(Material.AIR);
        ItemStack normalStick = new ItemStack(Material.STICK);

        // Shaped
        {
            RecipeType.VANILLA_CRAFTING_TABLE.addRecipe(
                    new ShapedRecipe(PylonTest.key("sticky_stick_shapeless"), result)
                            .shape(
                                    " s ",
                                    "sSs",
                                    " s "
                            )
                            .setIngredient('s', Material.STICK)
                            .setIngredient('S', stickyStickStick)
            );
            ItemStack[] crafting = new ItemStack[]{
                    nothing, normalStick, nothing,
                    normalStick, stickyStickStick, normalStick,
                    nothing, normalStick, nothing
            };
            assertThat(Bukkit.craftItem(crafting, PylonTest.testWorld))
                    .isEqualTo(result);
        }

        // Shapeless
        {
            RecipeType.VANILLA_CRAFTING_TABLE.addRecipe(
                    new ShapelessRecipe(PylonTest.key("sticky_stick_shaped"), result)
                            .addIngredient(Material.STICK)
                            .addIngredient(stickyStickStick)
                            .addIngredient(Material.STICK)
            );
            ItemStack[] crafting = new ItemStack[9];
            Arrays.fill(crafting, nothing);
            crafting[0] = stickyStickStick;
            crafting[1] = normalStick;
            assertThat(Bukkit.craftItem(crafting, PylonTest.testWorld))
                    .isEqualTo(result);
        }
    }
}
