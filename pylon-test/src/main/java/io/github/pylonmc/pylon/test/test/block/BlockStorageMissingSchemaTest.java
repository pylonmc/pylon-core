package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.block.PhantomBlock;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageMissingSchemaTest extends AsyncTest {

    private static final NamespacedKey BLOCK_KEY = PylonTest.key("block_storage_addon_reload_test");

    @Override
    protected void test() {
        PylonBlock.register(BLOCK_KEY, Material.AMETHYST_BLOCK, PylonBlock.class);

        Chunk chunk = TestUtil.getRandomChunk(false).join();
        Block block = chunk.getBlock(7, 100, 7);

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            BlockStorage.placeBlock(block, BLOCK_KEY);
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .isInstanceOf(PylonBlock.class);
        }).join();
        TestUtil.unloadChunk(chunk).join();

        TestUtil.runSync(() -> {
            assertThat(BlockStorage.isPylonBlock(block)).isFalse();
            PylonRegistry.BLOCKS.unregister(BLOCK_KEY);
        }).join();

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .isInstanceOf(PhantomBlock.class);
        }).join();
        TestUtil.unloadChunk(chunk).join();

        TestUtil.runSync(() -> {
            assertThat(BlockStorage.isPylonBlock(block)).isFalse();
            PylonBlock.register(BLOCK_KEY, Material.AMETHYST_BLOCK, PylonBlock.class);
        }).join();

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .isInstanceOf(PylonBlock.class);
        }).join();
        TestUtil.unloadChunk(chunk).join();
    }
}
