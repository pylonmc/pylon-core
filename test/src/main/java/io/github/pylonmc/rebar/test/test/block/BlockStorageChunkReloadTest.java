package io.github.pylonmc.rebar.test.test.block;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.RebarBlockSchema;
import io.github.pylonmc.rebar.test.base.AsyncTest;
import io.github.pylonmc.rebar.test.block.BlockWithField;
import io.github.pylonmc.rebar.test.util.TestUtil;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class BlockStorageChunkReloadTest extends AsyncTest {

    @Override
    public void test() {
        List<Chunk> chunks = TestUtil.getRandomChunks(20, false).join();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Chunk chunk : chunks) {
            Block block = chunk.getBlock(7, 100, 7);

            futures.add(TestUtil.runAsync(() -> {
                TestUtil.loadChunk(chunk).join();
                TestUtil.runSync(() -> {
                    BlockStorage.placeBlock(block, BlockWithField.KEY);
                }).join();
                TestUtil.unloadChunk(chunk).join();

                assertThat(new ChunkPosition(block).isLoaded())
                        .isFalse();
                assertThat(BlockStorage.isRebarBlock(block))
                        .isFalse();
                assertThatThrownBy(() -> BlockStorage.get(block))
                        .isNotNull();

                TestUtil.loadChunk(chunk).join();
                assertThat(BlockStorage.get(block))
                        .isNotNull()
                        .isInstanceOf(BlockWithField.class);

                assertThat(BlockStorage.get(block))
                        .isNotNull()
                        .extracting(RebarBlock::getSchema)
                        .isInstanceOf(RebarBlockSchema.class);

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
