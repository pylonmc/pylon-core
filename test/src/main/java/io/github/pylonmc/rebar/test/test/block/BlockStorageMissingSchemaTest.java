package io.github.pylonmc.rebar.test.test.block;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.PhantomBlock;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.test.RebarTest;
import io.github.pylonmc.rebar.test.base.AsyncTest;
import io.github.pylonmc.rebar.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageMissingSchemaTest extends AsyncTest {

    private static final NamespacedKey BLOCK_KEY = RebarTest.key("block_storage_addon_reload_test");

    @Override
    protected void test() {
        TestUtil.runSync(() -> {
                    RebarBlock.register(BLOCK_KEY, Material.AMETHYST_BLOCK, RebarBlock.class);
        }).join();

        Chunk chunk = TestUtil.getRandomChunk(false).join();
        Block block = chunk.getBlock(7, 100, 7);

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            BlockStorage.placeBlock(block, BLOCK_KEY);
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .isInstanceOf(RebarBlock.class);
        }).join();
        TestUtil.unloadChunk(chunk).join();

        TestUtil.runSync(() -> {
            assertThat(BlockStorage.isRebarBlock(block)).isFalse();
            RebarRegistry.BLOCKS.unregister(BLOCK_KEY);
        }).join();

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .isInstanceOf(PhantomBlock.class);
        }).join();
        TestUtil.unloadChunk(chunk).join();

        TestUtil.runSync(() -> {
            assertThat(BlockStorage.isRebarBlock(block)).isFalse();
            RebarBlock.register(BLOCK_KEY, Material.AMETHYST_BLOCK, RebarBlock.class);
        }).join();

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            assertThat(BlockStorage.get(block))
                    .isNotNull()
                    .isInstanceOf(RebarBlock.class);
        }).join();
        TestUtil.unloadChunk(chunk).join();
    }
}
