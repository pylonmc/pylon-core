package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class TestAddon extends JavaPlugin implements PylonAddon {

    World testWorld;

    @Override
    public void onEnable() {
        getLogger().info("Test addon enabled!");

        testWorld = new WorldCreator("gametests")
                .generator(new BedrockWorldGenerator())
                .environment(World.Environment.NORMAL)
                .createWorld();
        assert testWorld != null; // shut up intellij

        testWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        testWorld.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        testWorld.setGameRule(GameRule.DO_TRADER_SPAWNING, false);


        GameTests.setUpGameTests(this);
        GameTests.runGameTests(this);
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }
}
