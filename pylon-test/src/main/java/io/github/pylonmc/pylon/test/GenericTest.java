package io.github.pylonmc.pylon.test;

import org.bukkit.NamespacedKey;

public interface GenericTest {
    default NamespacedKey getKey() {
        return new NamespacedKey(TestAddon.instance(), this.getClass().getSimpleName());
    }
    void run();
    void cleanup();
}
