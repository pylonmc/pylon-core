package io.github.pylonmc.pylon.test.base;

import org.bukkit.NamespacedKey;

public record TestResult(NamespacedKey key, boolean success) {}
