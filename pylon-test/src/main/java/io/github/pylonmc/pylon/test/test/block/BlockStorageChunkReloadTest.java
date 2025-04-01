package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent;
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksLoadEvent;
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksUnloadEvent;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.block.BlockWithField;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.util.TestUtil;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageChunkReloadTest extends AsyncTest {

    private static final Map<Chunk, CompletableFuture<@Nullable Throwable>> blockLoadedFutures = new HashMap<>();
    private static final Set<Chunk> stage1Chunks = new HashSet<>();
    private static final Set<Chunk> stage2Chunks = new HashSet<>();
    private static final Set<Chunk> stage3Chunks = new HashSet<>();

    private static class TestListener implements Listener {

        /**
         * Stage 1: set the Pylon block when the Pylon data for that chunk is loaded, then unload the chunk.
         */
        @EventHandler
        public static void stage1(@NotNull PylonChunkBlocksLoadEvent e) {
            if (!stage1Chunks.contains(e.getChunk())) {
                return;
            }

            stage1Chunks.remove(e.getChunk());
            stage2Chunks.add(e.getChunk());

            TestUtil.runSync(() -> {
                BlockStorage.placeBlock(e.getChunk().getBlock(7, 100, 7), Blocks.BLOCK_WITH_FIELD);
                e.getChunk().unload();
            }, 10);
        }

        /**
         * Stage 2: When the chunk's pylon data is unloaded, check the block is gone, and load the chunk again.
         */
        @EventHandler
        public static void stage2(@NotNull PylonChunkBlocksUnloadEvent e) {
            if (!stage2Chunks.contains(e.getChunk())) {
                return;
            }

            stage2Chunks.remove(e.getChunk());
            stage3Chunks.add(e.getChunk());

            try {
                PylonBlock<?> pylonBlock = BlockStorage.get(e.getChunk().getBlock(7, 100, 7));

                assertThat(pylonBlock)
                        .isNull();

            } catch (Throwable t) {
                blockLoadedFutures.get(e.getChunk())
                        .complete(t);
            }

            TestUtil.runSync(() -> e.getChunk().load(), 10);
        }

        /**
         * Stage 3: Get the pylon block data that should be loaded when the chunk isloaded.
         */
        @EventHandler
        public static void stage3(@NotNull PylonBlockLoadEvent e) {
            if (!stage3Chunks.contains(e.getBlock().getChunk())) {
                return;
            }

            stage3Chunks.remove(e.getBlock().getChunk());

            Throwable exception = null;
            try {
                PylonBlock<?> pylonBlock = BlockStorage.get(e.getBlock());

                assertThat(pylonBlock)
                        .isNotNull()
                        .isInstanceOf(BlockWithField.class);

                assertThat(pylonBlock.getSchema())
                        .isInstanceOf(PylonBlockSchema.class);

                assertThat(BlockStorage.getAs(BlockWithField.class, e.getBlock()))
                        .isNotNull()
                        .extracting(block -> block.getProgress())
                        .isEqualTo(130);

            } catch (Throwable t) {
                exception = t;
            }

            blockLoadedFutures.get(e.getBlock().getChunk())
                    .complete(exception);
        }
    }

    @Override
    public void test() {
        List<Chunk> chunks = TestUtil.getRandomChunks(20, 20, true).join();

        stage1Chunks.addAll(chunks);

        for (Chunk chunk : chunks) {
            blockLoadedFutures.put(chunk, new CompletableFuture<>());
        }

        Bukkit.getPluginManager().registerEvents(new TestListener(), PylonTest.instance());

        for (Chunk chunk : chunks) {
            TestUtil.runSync(() -> chunk.load());
        }

        for (CompletableFuture<@Nullable Throwable> future : blockLoadedFutures.values()) {
            Throwable throwable = future.join();
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
