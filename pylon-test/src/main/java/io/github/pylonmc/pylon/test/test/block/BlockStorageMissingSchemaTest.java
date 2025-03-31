package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent;
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksLoadEvent;
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksUnloadEvent;
import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.block.PhantomBlock;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageMissingSchemaTest extends AsyncTest {
    public static class TestBlockSchema extends PylonBlockSchema {
        private final int processingSpeed;

        public TestBlockSchema(
                NamespacedKey key,
                Material material,
                Class<? extends PylonBlock<? extends PylonBlockSchema>> blockClass,
                int processingSpeed
        ) {
            super(key, material, blockClass);
            this.processingSpeed = processingSpeed;
        }
    }

    public static class TestBlock extends PylonBlock<TestBlockSchema> {
        public TestBlock(TestBlockSchema schema, Block block, BlockCreateContext context) {
            super(schema, block);
        }

        public TestBlock(
                TestBlockSchema schema,
                Block block,
                PersistentDataContainer pdc) {
            super(schema, block);
        }
    }

    private static final TestBlockSchema schema = new TestBlockSchema (
            PylonTest.key("block_storage_addon_reload_test"),
            Material.AMETHYST_BLOCK,
            TestBlock.class,
            12
    );

    private static Block block;
    private static int stage = 1;
    @NotNull private static final CompletableFuture<Throwable> future = new CompletableFuture<>();

    private static class TestListener implements Listener {

        /**
         * Stage 1: set the Pylon block when the Pylon data for that chunk is loaded, then unload the chunk
         */
        @EventHandler
        public static void stage1(PylonChunkBlocksLoadEvent e) {
            if (stage != 1 || !block.getChunk().equals(e.getChunk())) {
                return;
            }

            stage = 2;

            Bukkit.getScheduler().runTaskLater(PylonTest.instance(), () -> {
                BlockStorage.placeBlock(e.getChunk().getBlock(7, 100, 7), schema);
                e.getChunk().unload();
            }, 10);
        }

        /**
         * Stage 2: When the chunk's pylon data is unloaded, unregister the schema, then load the chunk again.
         */
        @EventHandler
        public static void stage2(PylonChunkBlocksUnloadEvent e) {
            if (stage != 2 || !block.getChunk().equals(e.getChunk())) {
                return;
            }

            stage = 3;

            Bukkit.getScheduler().runTaskLater(PylonTest.instance(), () -> {
                PylonRegistry.BLOCKS.unregister(schema);
                e.getChunk().load();
            }, 10);
        }

        /**
         * Stage 3: Get the pylon block data and check it's a phantom block, then unload the chunk
         */
        @EventHandler
        public static void stage3(PylonBlockLoadEvent e) {
            if (stage != 3 || !block.equals(e.getBlock())) {
                return;
            }

            stage = 4;

            try {
                PylonBlock<?> pylonBlock = BlockStorage.get(e.getBlock());

                assertThat(pylonBlock)
                        .isNotNull()
                        .isInstanceOf(PhantomBlock.class);

            } catch (Throwable t) {
                future.complete(t);
            }

            Bukkit.getScheduler().runTaskLater(PylonTest.instance(), () -> e.getBlock().getChunk().unload(), 10);
        }

        /**
         * Stage 4: When the chunk's pylon data is unloaded, reregister the schema, then load the chunk again.
         */
        @EventHandler
        public static void stage4(PylonChunkBlocksUnloadEvent e) {
            if (stage != 4 || !block.getChunk().equals(e.getChunk())) {
                return;
            }

            stage = 5;

            Bukkit.getScheduler().runTaskLater(PylonTest.instance(), () -> {
                schema.register();
                e.getChunk().load();
            }, 10);
        }

        /**
         * Stage 5: Get the pylon block data and check it's back to a TestBlock
         */
        @EventHandler
        public static void stage5(PylonBlockLoadEvent e) {
            if (stage != 5 || !block.equals(e.getBlock())) {
                return;
            }

            try {
                PylonBlock<?> pylonBlock = BlockStorage.get(e.getBlock());

                assertThat(pylonBlock)
                        .isNotNull()
                        .isInstanceOf(TestBlock.class);

            } catch (Throwable t) {
                future.complete(t);
            }

            future.complete(null);
        }
    }

    @Override
    protected void test() {
        schema.register();

        Chunk chunk = TestUtil.getRandomChunk(PylonTest.testWorld);
        block = chunk.getBlock(7, 100, 7);

        Bukkit.getPluginManager().registerEvents(new TestListener(), PylonTest.instance());

        Bukkit.getScheduler().runTask(PylonTest.instance(), () -> chunk.load());

        Throwable t = future.join();
        if (t != null) {
            throw new RuntimeException(t);
        }
    }
}
