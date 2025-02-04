package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.TestUtil;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;


public class BlockStorageChunkReloadTest extends AsyncTest {
    public static class TestBlock extends PylonBlock<PylonBlockSchema> {
        private final NamespacedKey somethingKey = PylonTest.key("something");

        private final int something;

        public TestBlock(@NotNull PylonBlockSchema schema, @NotNull Block block) {
            super(schema, block);
            something = 170;
        }

        public TestBlock(
                @NotNull PylonBlockSchema schema,
                @NotNull PersistentDataContainer pdc,
                @NotNull Block block
        ) {
            super(schema, block);
            something = pdc.get(somethingKey, PylonSerializers.INTEGER);
        }

        @Override
        public void write(@NotNull PersistentDataContainer pdc) {
            pdc.set(somethingKey, PylonSerializers.INTEGER, something - 40);
        }
    }

    private static final PylonBlockSchema schema = new PylonBlockSchema  (
            PylonTest.key("block_storage_chunk_reload_test"),
            Material.AMETHYST_BLOCK,
            TestBlock.class
    );

    private static final Map<Chunk, CompletableFuture<Throwable>> blockLoadedFutures = new HashMap<>();
    private static final Set<Chunk> stage1Chunks = new HashSet<>();
    private static final Set<Chunk> stage2Chunks = new HashSet<>();
    private static final Set<Chunk> stage3Chunks = new HashSet<>();

    private static class TestListener implements Listener {
        /**
         * Stage 1: set the Pylon block when the Pylon data for that chunk is loaded, then unload the chunk.
         */
        @EventHandler
        public static void stage1(@NotNull ChunkLoadEvent e) {
            if (!stage1Chunks.contains(e.getChunk())) {
                return;
            }

            stage1Chunks.remove(e.getChunk());
            stage2Chunks.add(e.getChunk());

            // Run this later to prevent stage 3 from firing early
            Bukkit.getScheduler().runTaskLater(PylonTest.instance(), () -> {
                BlockStorage.set(e.getChunk().getBlock(7, 100, 7), schema);
                e.getChunk().unload();
            }, 1);
        }

        /**
         * Stage 2: When the chunk's pylon data is unloaded, load the chunk again.
         */
        @EventHandler
        public static void stage2(@NotNull ChunkUnloadEvent e) {
            if (!stage2Chunks.contains(e.getChunk())) {
                return;
            }

            stage2Chunks.remove(e.getChunk());
            stage3Chunks.add(e.getChunk());

            e.getChunk().load();
        }

        /**
         * Stage 3: Get the pylon block data that should be loaded when the chunk isloaded.
         */
        @EventHandler
        public static void stage3(@NotNull PylonBlockLoadEvent e) {
            if (!stage3Chunks.contains(e.getBlock().getChunk())) {
                return;
            }

            Throwable exception = null;
            try {
                PylonBlock<PylonBlockSchema> pylonBlock = BlockStorage.get(e.getBlock());

                assertThat(pylonBlock)
                        .isNotNull()
                        .isInstanceOf(TestBlock.class);

                assertThat(pylonBlock.getSchema())
                        .isInstanceOf(PylonBlockSchema.class);

                assertThat(BlockStorage.getAs(TestBlock.class, e.getBlock()))
                        .isNotNull()
                        .extracting(block -> block.something)
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
        schema.register();

        List<Chunk> chunks = TestUtil.getRandomChunks(PylonTest.testWorld, 20, 20);

        stage1Chunks.addAll(chunks);

        for (Chunk chunk : chunks) {
            blockLoadedFutures.put(chunk, new CompletableFuture<>());
        }

        Bukkit.getPluginManager().registerEvents(new TestListener(), PylonTest.instance());

        for (Chunk chunk : chunks) {
            Bukkit.getScheduler().runTask(PylonTest.instance(), () -> chunk.load());
        }

        for (CompletableFuture<Throwable> future : blockLoadedFutures.values()) {
            Throwable throwable = future.join();
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
