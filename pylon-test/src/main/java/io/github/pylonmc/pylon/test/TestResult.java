package io.github.pylonmc.pylon.test;

import org.bukkit.NamespacedKey;

public record TestResult(NamespacedKey key, boolean success) {}
