package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageMissingSchemaTest extends AsyncTest {

    private static final PylonBlockSchema schema = PylonBlockSchema.simple(
            PylonTest.key("block_storage_addon_reload_test"),
            Material.AMETHYST_BLOCK,
            PylonBlock::new
    );

    @Override
    protected void test() {
        schema.register();

        Chunk chunk = TestUtil.getRandomChunk(false).join();
        Block block = chunk.getBlock(7, 100, 7);

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            BlockStorage.placeBlock(block, schema);
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .extracting(PylonBlock::getSchema)
                    .extracting(PylonBlockSchema::getKey)
                    .isEqualTo(schema.getKey());
        }).join();
        TestUtil.unloadChunk(chunk).join();

        TestUtil.runSync(() -> {
            assertThat(BlockStorage.isPylonBlock(block)).isFalse();
            PylonRegistry.BLOCKS.unregister(schema);
        }).join();

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .isInstanceOf(PylonBlock.class);
        }).join();
        TestUtil.unloadChunk(chunk).join();

        TestUtil.runSync(() -> {
            assertThat(BlockStorage.isPylonBlock(block)).isFalse();
            schema.register();
        }).join();

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            BlockStorage.placeBlock(block, schema);
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .extracting(PylonBlock::getSchema)
                    .extracting(PylonBlockSchema::getKey)
                    .isEqualTo(schema.getKey());
        }).join();
        TestUtil.unloadChunk(chunk).join();
    }
}
