package io.github.pylonmc.pylon.test;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;


public class TestUtil {
    private static final Random random = new Random();

    private static @NotNull CompletableFuture<Chunk> getRandomChunk(@NotNull World world) {
        TestAddon.instance().getLogger().severe("iofhigowe2");
        double max = 10000;
        int x = (int) random.nextDouble(-max, max);
        int z = (int) random.nextDouble(-max, max);
        return world.getChunkAtAsync(x, z);
    }

    public static @NotNull List<Chunk> getRandomChunks(@NotNull World world, int count) {
        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            futures.add(getRandomChunk(world));
        }

        List<Chunk> chunks = new ArrayList<>();
        for (CompletableFuture<Chunk> future : futures) {
            chunks.add(future.join());
            TestAddon.instance().getLogger().severe(chunks.toString());
        }
        TestAddon.instance().getLogger().severe("AAAAAAAA");

        return chunks;
    }
}
