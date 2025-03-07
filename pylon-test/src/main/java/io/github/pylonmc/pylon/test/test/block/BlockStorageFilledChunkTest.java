package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.SimplePylonBlock;
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent;
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksLoadEvent;
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksUnloadEvent;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.TestUtil;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@NotNullByDefault
public class BlockStorageFilledChunkTest extends AsyncTest {
    private static final PylonBlockSchema schema = new PylonBlockSchema(
            PylonTest.key("block_storage_fill_chunk_test"),
            Material.AMETHYST_BLOCK,
            SimplePylonBlock.class
    );

    private static final Map<BlockPosition, CompletableFuture<Throwable>> blockLoadedFutures = new HashMap<>();
    private static Chunk chunk;
    private static int stage = 1;

    private static class TestListener implements Listener {
        /**
         * Stage 1: set the Pylon block when the Pylon data for that chunk is loaded, then unload the chunk.
         */
        @EventHandler
        public static void stage1(PylonChunkBlocksLoadEvent e) {
            if (stage != 1 || !e.getChunk().equals(chunk)) {
                return;
            }

            stage = 2;

            // Run this later to prevent stage 3 from firing early
            Bukkit.getScheduler().runTaskLater(PylonTest.instance(), () -> {
                for (BlockPosition blockPosition : blockLoadedFutures.keySet()) {
                    BlockStorage.placeBlock(blockPosition, schema);
                }
                e.getChunk().unload();
            }, 10);
        }

        /**
         * Stage 2: When the chunk's pylon data is unloaded, load the chunk again.
         */
        @EventHandler
        public static void stage2(PylonChunkBlocksUnloadEvent e) {
            if (stage != 2 || !e.getChunk().equals(chunk)) {
                return;
            }

            stage = 3;

            Bukkit.getScheduler().runTaskLater(PylonTest.instance(), () -> e.getChunk().load(), 10);
        }

        /**
         * Stage 3: Get the pylon block data that should be loaded when the chunk isloaded.
         */
        @EventHandler
        public static void stage3(PylonBlockLoadEvent e) {
            if (stage != 3 || !e.getBlock().getChunk().equals(chunk)) {
                return;
            }

            Throwable exception = null;
            try {
                PylonBlock<PylonBlockSchema> pylonBlock = BlockStorage.get(e.getBlock());

                assertThat(pylonBlock)
                        .isNotNull();

                assertThat(pylonBlock.getSchema())
                        .isEqualTo(schema);

            } catch (Throwable t) {
                exception = t;
            }

            blockLoadedFutures.get(new BlockPosition(e.getBlock()))
                    .complete(exception);
        }
    }

    @Override
    public void test() {
        schema.register();

        chunk = TestUtil.getRandomChunk(PylonTest.testWorld);

        Bukkit.getPluginManager().registerEvents(new TestListener(), PylonTest.instance());

        for (int x = 0; x < 16; x++) {
            for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPosition blockPosition = new BlockPosition(chunk.getBlock(x, y, z));
                    blockLoadedFutures.put(blockPosition, new CompletableFuture<>());
                }
            }
        }

        Bukkit.getScheduler().runTask(PylonTest.instance(), () -> chunk.load());

        for (CompletableFuture<Throwable> future : blockLoadedFutures.values()) {
            Throwable throwable = future.join();
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
