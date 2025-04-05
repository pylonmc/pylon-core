package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import io.github.pylonmc.pylon.test.base.Test;
import io.github.pylonmc.pylon.test.base.TestResult;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.entity.Entities;
import io.github.pylonmc.pylon.test.item.Items;
import io.github.pylonmc.pylon.test.test.block.*;
import io.github.pylonmc.pylon.test.test.entity.EntityStorageChunkReloadTest;
import io.github.pylonmc.pylon.test.test.entity.EntityStorageMissingSchemaTest;
import io.github.pylonmc.pylon.test.test.entity.EntityStorageSimpleTest;
import io.github.pylonmc.pylon.test.test.item.PylonItemStackInterfaceTest;
import io.github.pylonmc.pylon.test.test.misc.GametestTest;
import io.github.pylonmc.pylon.test.test.recipe.CraftingTest;
import io.github.pylonmc.pylon.test.test.recipe.FurnaceTest;
import io.github.pylonmc.pylon.test.test.recipe.MobDropTest;
import io.github.pylonmc.pylon.test.test.serializer.*;
import io.github.pylonmc.pylon.test.util.BedrockWorldGenerator;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PylonTest extends JavaPlugin implements PylonAddon {
    private static PylonTest instance;
    public static World testWorld;
    private static final boolean MANUAL_SHUTDOWN = Boolean.parseBoolean(System.getenv("MANUAL_SHUTDOWN"));

    @Override
    public @NotNull String displayName() {
        return "Pylon Test";
    }

    private static @NotNull List<Test> initTests() {
        List<Test> tests = new ArrayList<>();

        tests.add(new BlockStorageAddTest());
        tests.add(new BlockStorageChunkReloadTest());
        tests.add(new BlockStorageFilledChunkTest());
        tests.add(new BlockStorageMissingSchemaTest());
        tests.add(new BlockStorageRemoveTest());
        tests.add(new SimpleMultiblockTest());
        tests.add(new SimpleMultiblockRotatedTest());
        tests.add(new TickingBlockTest());
        tests.add(new TickingBlockErrorTest());

        tests.add(new PylonItemStackInterfaceTest());

        tests.add(new GametestTest());

        tests.add(new SerializerTestBlockPosition());
        tests.add(new SerializerTestBlockPositionNoWorld());
        tests.add(new SerializerTestChar());
        tests.add(new SerializerTestChunkPosition());
        tests.add(new SerializerTestChunkPositionNoWorld());
        tests.add(new SerializerTestEnum());
        tests.add(new SerializerTestItemStack());
        tests.add(new SerializerTestLocation());
        tests.add(new SerializerTestNamespacedKey());
        tests.add(new SerializerTestSetOfInts());
        tests.add(new SerializerTestSetOfSetOfStrings());
        tests.add(new SerializerTestUUID());
        tests.add(new SerializerTestVector());

        tests.add(new CraftingTest());
        tests.add(new FurnaceTest());
        tests.add(new MobDropTest());

        tests.add(new EntityStorageSimpleTest());
        tests.add(new EntityStorageMissingSchemaTest());
        tests.add(new EntityStorageChunkReloadTest());

        return tests;
    }

    private static void run() {
        Logger logger = instance.getLogger();

        // Create world - can't do this on enable due to plugin loading on STARTUP rather than POSTWORLD
        TestUtil.runSync(() -> {
            World world = new WorldCreator("gametests")
                    .generator(new BedrockWorldGenerator())
                    .environment(World.Environment.NORMAL)
                    .createWorld();
            assert world != null; // shut up intellij // you can't make me, you fool. I will destroy you silly mortals.
            testWorld = world;

            testWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            testWorld.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            testWorld.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        }).join();

        try {
            Blocks.register();
            Items.register();
            Entities.register();
        } catch (Exception e) {
            instance().getLogger().severe("Failed to set up tests");
            e.printStackTrace();
            communicateFailure();
            if (!MANUAL_SHUTDOWN) {
                Bukkit.shutdown();
            }
        }

        // Tests must be initialised on main thread
        List<Test> tests = TestUtil.runSync(PylonTest::initTests).join();

        List<TestResult> results = tests.stream()
                .map(Test::run)
                .toList();

        List<NamespacedKey> succeeded = results.stream()
                .filter(TestResult::success)
                .map(TestResult::key)
                .toList();
        List<NamespacedKey> failed = results.stream()
                .filter(r -> !r.success())
                .map(TestResult::key)
                .toList();


        logger.info("[ ===== TEST SUMMARY ===== ]");
        logger.info("%s/%s TESTS PASSED"
                .formatted(succeeded.size(), succeeded.size() + failed.size()));

        if (failed.isEmpty()) {
            long totalTimeTakenMillis = results.stream()
                    .mapToLong(TestResult::timeTakenMillis)
                    .sum();
            logger.info("TIME TAKEN: %dms".formatted(totalTimeTakenMillis));
        }

        if (!failed.isEmpty()) {
            String failedString = failed.stream()
                    .map(NamespacedKey::toString)
                    .collect(Collectors.joining(", "));
            logger.info("FAILED: %s".formatted(failedString));

            communicateFailure();
        }

        if (!MANUAL_SHUTDOWN) {
            logger.info("Testing complete; shutting down server...");
            Bukkit.shutdown();
        }
    }

    private static void communicateFailure() {
        // Communicate back to the runServer task that the tests failed
        try {
            new File("tests-failed").createNewFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void onEnable() {
        registerWithPylon();

        getLogger().info("Test addon enabled!");

        instance = this;

        if (Boolean.parseBoolean(System.getenv("NO_TEST"))) {
            return;
        }

        TestUtil.runAsync(PylonTest::run, 1);
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }

    public static PylonTest instance() {
        return instance;
    }

    public static @NotNull NamespacedKey key(String key) {
        return new NamespacedKey(instance, key);
    }
}
