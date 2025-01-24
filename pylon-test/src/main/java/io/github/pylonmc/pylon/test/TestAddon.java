package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class TestAddon extends JavaPlugin implements PylonAddon {
    private static TestAddon instance;
    public static World testWorld;

    @Override
    public void onEnable() {
        getLogger().info("Test addon enabled!");

        instance = this;

        testWorld = new WorldCreator("gametests")
                .generator(new BedrockWorldGenerator())
                .environment(World.Environment.NORMAL)
                .createWorld();
        assert testWorld != null; // shut up intellij // you can't make me, you fool. I will destroy you silly mortals.

        testWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        testWorld.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        testWorld.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

        GenericTests.setUpGenericTests();
        var futures = GenericTests.runGenericTests();

        GameTests.setUpGameTests();
        futures.addAll(GameTests.runGameTests());

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    TestAddon.instance().getLogger().info("All gametests complete!");
                    if (!Boolean.parseBoolean(System.getenv("MANUAL_SHUTDOWN"))) {
                        Bukkit.shutdown();
                    }
                });
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }

    public static TestAddon instance() {
        return instance;
    }
}
