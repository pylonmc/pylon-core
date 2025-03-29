package io.github.pylonmc.pylon.test.test.item;

import io.github.pylonmc.pylon.core.item.SimpleItemSchema;
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder;
import io.github.pylonmc.pylon.core.recipe.RecipeTypes;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import org.bukkit.Material;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;


public class PylonItemStackSimpleTest extends SyncTest {
    @Override
    public void test() {
        new SimpleItemSchema<>(
                PylonTest.key("pylon_item_stack_simple_test"),
                new ItemStackBuilder(Material.BLAZE_POWDER)
                        .name("<#ff0000>OMINOUS BLAZE POWER")
                        .lore("<#ff0000>VERY SCARY")
                        .lore("<#222222>OH NO")
                        .build(),
                RecipeTypes.VANILLA_BLASTING,
                stack -> new BlastingRecipe(
                        PylonTest.key("pylon_item_stack_simple_test"),
                        stack,
                        new RecipeChoice.ExactChoice(new ItemStack(Material.BLAZE_POWDER)),
                        5.0F,
                        20)
        ).register();
    }
}
