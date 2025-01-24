package io.github.pylonmc.pylon.test.gametest;

import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.jetbrains.annotations.NotNull;

public class GametestTest {
    public static @NotNull GameTestConfig get() {
        return new GameTestConfig.Builder(new NamespacedKey(TestAddon.instance(), "test"))
                .size(1)
                .setUp((test) -> {
                    test.getWorld().spawn(test.location(1.5, 0, 0), Fox.class);
                    test.getWorld().spawn(test.location(1.5, 0, 1), Chicken.class);
                    test.succeedWhen(() -> !test.entityInBounds((entity) -> entity.getType() == EntityType.CHICKEN));
                })
                .build();
    }
}
