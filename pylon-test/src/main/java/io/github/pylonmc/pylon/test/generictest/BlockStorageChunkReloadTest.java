package io.github.pylonmc.pylon.test.generictest;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent;
import io.github.pylonmc.pylon.core.persistence.BlockStorage;
import io.github.pylonmc.pylon.core.persistence.PylonDataReader;
import io.github.pylonmc.pylon.core.persistence.PylonDataWriter;
import io.github.pylonmc.pylon.core.persistence.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import io.github.pylonmc.pylon.test.TestUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;


public class BlockStorageChunkReloadTest implements GenericTest {
    public static class TestBlock extends PylonBlock<PylonBlockSchema> {
        private final NamespacedKey somethingKey = TestAddon.key("something");

        private final int something;

        public TestBlock(@NotNull PylonBlockSchema schema, @NotNull Block block) {
            super(schema, block);
            something = 170;
        }

        public TestBlock(@NotNull PylonDataReader reader, @NotNull Block block) {
            super(reader, block);
            something = reader.get(somethingKey, PylonSerializers.INTEGER);
        }

        @Override
        public void write(@NotNull PylonDataWriter writer) {
            writer.set(somethingKey, PylonSerializers.INTEGER, something - 40);
        }
    }

    private static final PylonBlockSchema schema = new PylonBlockSchema  (
            TestAddon.key("block_storage_chunk_reload_test"),
            Material.AMETHYST_BLOCK,
            TestBlock.class
    );

    private static final Map<Chunk, CompletableFuture<Throwable>> blockLoadedFutures = new HashMap<>();

    private static class TestListener implements Listener {
        private static @NotNull Block getBlockFromChunk(@NotNull Chunk chunk) {
            return chunk.getBlock(7, 100, 7);
        }

        @EventHandler
        public static void onChunkLoad(@NotNull ChunkLoadEvent e) {
            if (!blockLoadedFutures.containsKey(e.getChunk())) {
                return;
            }

            TestAddon.instance().getLogger().severe("1");

            BlockStorage.set(getBlockFromChunk(e.getChunk()), schema);

            e.getChunk().unload();
        }

        @EventHandler
        public static void onChunkUnload(@NotNull ChunkUnloadEvent e) {
            if (!blockLoadedFutures.containsKey(e.getChunk())) {
                return;
            }

            TestAddon.instance().getLogger().severe("2");

            e.getChunk().load();
        }

        @EventHandler
        public static void onBlockStorageLoad(@NotNull PylonBlockLoadEvent e) {
            if (!blockLoadedFutures.containsKey(e.getBlock().getChunk())) {
                return;
            }

            TestAddon.instance().getLogger().severe("3");

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
            blockLoadedFutures.get(e.getBlock().getChunk()).complete(exception);
        }
    }

    @Override
    public void run() {
        schema.register();

        List<Chunk> chunks = TestUtil.getRandomChunks(TestAddon.testWorld, 256);

        for (Chunk chunk : chunks) {
            blockLoadedFutures.put(chunk, new CompletableFuture<>());
        }

        Bukkit.getPluginManager().registerEvents(new TestListener(), TestAddon.instance());

        for (Chunk chunk : chunks) {
            chunk.unload();
            chunk.load();
        }
        TestAddon.instance().getLogger().severe("brrrrrrrrrrr");

        for (CompletableFuture<Throwable> future : blockLoadedFutures.values()) {
            Throwable throwable = future.join();
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
