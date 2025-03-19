package io.github.pylonmc.pylon.test.test.item;

import io.github.pylonmc.pylon.core.item.ItemStackBuilder;
import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.item.SimpleItemSchema;
import io.github.pylonmc.pylon.core.recipe.RecipeTypes;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleItemSchemaTest extends SyncTest {

    @Override
    public void test() {
        PylonItemSchema primaryconstructor = new SimpleItemSchema<>(
                PylonTest.key("simple_item_schema_1"),
                new ItemStackBuilder(Material.ACACIA_BUTTON)
                        .name("A cool item")
                        .lore("Something cool")
                        .set(DataComponentTypes.RARITY, ItemRarity.RARE)
                        .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
                        .build(),
                RecipeTypes.VANILLA_CRAFTING,
                testitem -> {
                    ShapedRecipe recipe = new ShapedRecipe(PylonTest.key("simple_item_schema_1"), testitem);
                    recipe.shape(
                            "SSS",
                            "SSS",
                            "SSS"
                    );
                    recipe.setIngredient('S', Material.STICK);
                    recipe.setCategory(CraftingBookCategory.MISC);
                    return recipe;
                }
        );

        PylonItemSchema secondaryconstructor = new SimpleItemSchema<>(
                PylonTest.key("simple_item_schema_2"),
                new ItemStackBuilder(Material.ACACIA_BUTTON)
                        .name("A cool item")
                        .lore("Something cool")
                        .set(DataComponentTypes.RARITY, ItemRarity.RARE)
                        .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
                        .build(),
                RecipeTypes.VANILLA_CRAFTING,
                testitem -> {
                    ShapedRecipe recipe = new ShapedRecipe(PylonTest.key("simple_item_schema_2"), testitem);
                    recipe.shape(
                            "SSS",
                            "SSS",
                            "SSS"
                    );
                    recipe.setIngredient('S', Material.STICK);
                    recipe.setCategory(CraftingBookCategory.MISC);
                    return recipe;
                }
        );

        assertThat(primaryconstructor.getItemStack()).isEqualTo(secondaryconstructor.getItemStack());
    }
}
