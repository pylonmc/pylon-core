package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import io.github.pylonmc.pylon.test.base.Test;
import io.github.pylonmc.pylon.test.base.TestResult;
import io.github.pylonmc.pylon.test.test.block.*;
import io.github.pylonmc.pylon.test.test.item.PylonItemStackInterfaceTest;
import io.github.pylonmc.pylon.test.test.item.PylonItemStackSimpleTest;
import io.github.pylonmc.pylon.test.test.misc.GametestTest;
import io.github.pylonmc.pylon.test.test.pdc.PylonPDCPrimitivesTest;
import io.github.pylonmc.pylon.test.test.pdc.PylonPDCSerializationTest;
import io.github.pylonmc.pylon.test.test.serializer.*;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PylonTest extends JavaPlugin implements PylonAddon {
    private static PylonTest instance;
    public static World testWorld;

    private static @NotNull List<Test> initTests() {
        List<Test> tests = new ArrayList<>();

        tests.add(new BlockStorageAddTest());
        tests.add(new BlockStorageChunkReloadTest());
        tests.add(new BlockStorageFilledChunkTest());
        tests.add(new BlockStorageMissingSchemaTest());
        tests.add(new BlockStorageRemoveTest());
        tests.add(new SimpleBlockTest());
        tests.add(new SimpleBlockWithSchemaTest());

        tests.add(new PylonItemStackSimpleTest());
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

        tests.add(new PylonPDCPrimitivesTest());
        tests.add(new PylonPDCSerializationTest());
        tests.add(new PylonItemStackSimpleTest());
        tests.add(new PylonItemStackInterfaceTest());

        tests.add(new CraftingTest());

        return tests;
    }

    private void run() {
        CompletableFuture<List<Test>> testsFuture = new CompletableFuture<>();

        // Tests must be initialised on main thread
        Bukkit.getScheduler().runTask(this, () -> testsFuture.complete(initTests()));

        List<Test> tests = testsFuture.join();

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

        Logger logger = instance.getLogger();

        logger.info("[ ===== TEST SUMMARY ===== ]");
        logger.info("%s/%s TESTS PASSED"
                .formatted(succeeded.size(), succeeded.size() + failed.size()));

        if (!failed.isEmpty()) {
            String failedString = failed.stream()
                    .map(NamespacedKey::toString)
                    .collect(Collectors.joining(", "));
            logger.info("FAILED: %s".formatted(failedString));

            // Communicate back to the runServer task that the tests failed
            try {
                new File("tests-failed").createNewFile();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }


        if (!Boolean.parseBoolean(System.getenv("MANUAL_SHUTDOWN"))) {
            logger.info("Testing complete; shutting down server...");
            Bukkit.shutdown();
        }
    }

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

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, this::run, 1);
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
