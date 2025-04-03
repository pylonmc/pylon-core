package io.github.pylonmc.pylon.test.test.recipe;

import io.github.pylonmc.pylon.core.mobdrop.SimpleMobDrop;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.item.Items;
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
                    ItemStack stickyStick = Items.STICKY_STICK.getItemStack();
                    PylonRegistry.MOB_DROPS.register(new SimpleMobDrop(
                            PylonTest.key("sticky_stick"),
                            stickyStick,
                            EntityType.CHICKEN,
                            false
                    ));
                    test.getWorld().spawn(test.location(1.5, 0, 0), Fox.class);
                    test.getWorld().spawn(test.location(1.5, 0, 1), Chicken.class);
                    test.succeedWhen(() -> test.entityInBounds(entity ->
                            entity instanceof Item item && item.getItemStack().isSimilar(stickyStick))
                    );
                })
                .build());
    }
}
