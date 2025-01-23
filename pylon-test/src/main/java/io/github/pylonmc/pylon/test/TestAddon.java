package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.registry.PylonRegistries;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.registry.PyonRegistryKeys;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.core.test.GameTestFailException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

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
