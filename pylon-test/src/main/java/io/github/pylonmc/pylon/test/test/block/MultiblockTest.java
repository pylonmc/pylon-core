package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.block.SimpleTestMultiblock;
import io.github.pylonmc.pylon.test.util.TestUtil;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import org.bukkit.Chunk;
import org.bukkit.Location;

import static org.assertj.core.api.Assertions.assertThat;


public class MultiblockTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Location multiblockLocation = chunk.getBlock(4, 100, 4).getLocation();
        Location component1Location = multiblockLocation.clone().add(1, 1, 0);
        Location component2Location = multiblockLocation.clone().add(2, -1, 0);

        TestUtil.runSync(() -> BlockStorage.placeBlock(multiblockLocation, Blocks.SIMPLE_MULTIBLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(SimpleTestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isFalse());

        TestUtil.runSync(() -> BlockStorage.placeBlock(component1Location, Blocks.SIMPLE_BLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(SimpleTestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isFalse());

        TestUtil.runSync(() -> BlockStorage.placeBlock(component2Location, Blocks.SIMPLE_BLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(SimpleTestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isTrue());

        TestUtil.runSync(() -> BlockStorage.breakBlock(component2Location)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(SimpleTestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isFalse());

        TestUtil.runSync(() -> BlockStorage.placeBlock(component2Location, Blocks.SIMPLE_BLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(SimpleTestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isTrue());

        TestUtil.runSync(() -> {
            BlockStorage.breakBlock(multiblockLocation);
            BlockStorage.placeBlock(multiblockLocation, Blocks.SIMPLE_MULTIBLOCK);
        }).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(SimpleTestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isTrue());

        chunk.setForceLoaded(false);
    }
}
