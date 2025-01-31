package io.github.pylonmc.pylon.test.base;

import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;


public interface Test {
    TestResult run();

    default NamespacedKey getKey() {
        return new NamespacedKey(PylonTest.instance(), this.getClass().getSimpleName());
    }

    default @NotNull TestResult onComplete(@Nullable Throwable e) {
        if (e != null) {
            PylonTest.instance().getLogger().log(Level.SEVERE, "Test %s failed!".formatted(getKey()), e);
        }
        return new TestResult(getKey(), e == null);
    }
}
