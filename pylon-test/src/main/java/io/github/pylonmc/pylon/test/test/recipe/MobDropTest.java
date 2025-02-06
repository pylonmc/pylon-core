package io.github.pylonmc.pylon.test.test.recipe;

import io.github.pylonmc.pylon.core.recipe.MobDropRecipe;
import io.github.pylonmc.pylon.core.recipe.RecipeTypes;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class MobDropTest extends GameTest {
    public MobDropTest() {
        super(new GameTestConfig.Builder(PylonTest.key("mob_drop_test"))
                .size(1)
                .setUp((test) -> {
                    ItemStack stickyStick = StickyStick.INSTANCE.getItemStack();
                    if (stickyStick.getType() == Material.AIR) {
                        throw new IllegalStateException("Sticky stick is air");
                    }
                    RecipeTypes.MOB_DROP.addRecipe(new MobDropRecipe.Simple(
                            PylonTest.key("sticky_stick"),
                            stickyStick,
                            EntityType.CHICKEN,
                            false
                    ));
                    test.getWorld().spawn(test.location(1.5, 0, 0), Fox.class);
                    test.getWorld().spawn(test.location(1.5, 0, 1), Chicken.class);
                    test.succeedWhen(() -> test.entityInBounds(entity -> {
                        if (entity instanceof Item item) {
                            System.out.println("Dropped");
                            System.out.println(item.getItemStack());
                            System.out.println("Compared");
                            System.out.println(stickyStick);
                            return item.getItemStack().isSimilar(stickyStick);
                        } else {
                            return false;
                        }
                    }));
                })
                .build());
    }
}
