package io.github.pylonmc.rebar.test.base;

import org.bukkit.NamespacedKey;

public record TestResult(NamespacedKey key, boolean success, long timeTakenMillis) {}
