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

    public static @NotNull CompletableFuture<Chunk> getRandomChunk(@NotNull World world) {
        double max = 10000;
        int x = (int) random.nextDouble(-max, max);
        int z = (int) random.nextDouble(-max, max);
        return world.getChunkAtAsync(x, z);
    }

    public static @NotNull List<Chunk> getChunks(@NotNull World world, int width, int height) {
        double max = 10000;
        int startX = (int) random.nextDouble(-max, max);
        int startZ = (int) random.nextDouble(-max, max);

        // Get all chunks at the same time instead of waiting for each one to finish individually
        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (int x = startX; x < width; x++) {
            for (int z = startZ; z < width; z++) {
                futures.add(world.getChunkAtAsync(x, z));
            }
        }

        List<Chunk> chunks = new ArrayList<>();
        for (CompletableFuture<Chunk> future : futures) {
            chunks.add(future.join());
        }

        return chunks;
    }
}
