package io.github.pylonmc.pylon.test.base;

import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;


public interface Test {
    TestResult run();

    default NamespacedKey getKey() {
        return new NamespacedKey(PylonTest.instance(), getClass().getSimpleName());
    }

    default @NotNull TestResult onComplete(@Nullable Throwable e, Instant startTime) {
        long timeTakenMillis = Duration.between(startTime, Instant.now()).toMillis();
        if (e != null) {
            PylonTest.instance().getLogger().log(Level.INFO, "Test %s failed".formatted(getKey()), e);
        } else {
            PylonTest.instance().getLogger().log(Level.INFO, "Test %s passed in %dms".formatted(getKey(), timeTakenMillis));
        }
        return new TestResult(getKey(), e == null, timeTakenMillis);
    }
}
