package io.github.pylonmc.pylon.test.test.misc;

import io.github.pylonmc.rebar.gametest.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;

public class GametestTest extends GameTest {
    public GametestTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "test"))
                .size(1)
                .setUp((test) -> {
                    test.getWorld().spawn(test.location(1.5, 0, 0), Fox.class);
                    test.getWorld().spawn(test.location(1.5, 0, 1), Chicken.class);
                    test.succeedWhen(() -> !test.entityInBounds((entity) -> entity.getType() == EntityType.CHICKEN));
                })
                .build());
    }
}
