package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageFilledChunkTest extends AsyncTest {

    @Override
    public void test() {
        Chunk chunk = TestUtil.getRandomChunk(true).join();

        List<Block> blocks = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    blocks.add(chunk.getBlock(x, y, z));
                }
            }
        }

        TestUtil.loadChunk(chunk).join();
        TestUtil.runSync(() -> {
            for (Block block : blocks) {
                BlockStorage.placeBlock(block, Blocks.SIMPLE_BLOCK_KEY);
            }
        }).join();
        TestUtil.unloadChunk(chunk).join();

        TestUtil.loadChunk(chunk).join();
        for (Block block : blocks) {
            PylonBlock pylonBlock = BlockStorage.get(block);

            assertThat(pylonBlock)
                    .isNotNull();

            assertThat(pylonBlock.getSchema())
                    .extracting(PylonBlockSchema::getKey)
                    .isEqualTo(Blocks.SIMPLE_BLOCK_KEY);
        }
        TestUtil.unloadChunk(chunk).join();
    }
}
