package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.test.block.BlockWithField;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.util.TestUtil;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class BlockStorageChunkReloadTest extends AsyncTest {

    @Override
    public void test() {
        List<Chunk> chunks = TestUtil.getRandomChunks(20, 20, false).join();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Chunk chunk : chunks) {
            Block block = chunk.getBlock(7, 100, 7);

            futures.add(TestUtil.runAsync(() -> {
                TestUtil.loadChunk(chunk).join();
                TestUtil.runSync(() -> {
                    BlockStorage.placeBlock(block, Blocks.BLOCK_WITH_FIELD);
                }).join();
                TestUtil.unloadChunk(chunk).join();

                assertThat(BlockStorage.isPylonBlock(block))
                        .isFalse();
                assertThatThrownBy(() -> BlockStorage.get(block))
                        .isInstanceOf(IllegalArgumentException.class);

                TestUtil.loadChunk(chunk).join();
                assertThat(BlockStorage.get(block))
                        .isNotNull()
                        .isInstanceOf(BlockWithField.class);

                assertThat(BlockStorage.get(block))
                        .isNotNull()
                        .extracting(PylonBlock::getSchema)
                        .isInstanceOf(PylonBlockSchema.class);

                assertThat(BlockStorage.getAs(BlockWithField.class, block))
                        .isNotNull()
                        .extracting(BlockWithField::getProgress)
                        .isEqualTo(130);
                TestUtil.unloadChunk(chunk).join();
            }));
        }

        futures.forEach(CompletableFuture::join);
    }
}
