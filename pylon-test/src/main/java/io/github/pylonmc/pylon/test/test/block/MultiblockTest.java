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

    private static void assertMultiblockFormed(Location multiblockLocation, boolean formed) {
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(SimpleTestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isEqualTo(formed)
                );
    }

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Location multiblockLocation = chunk.getBlock(4, 100, 4).getLocation();
        Location component1Location = multiblockLocation.clone().add(1, 1, 0);
        Location component2Location = multiblockLocation.clone().add(2, -1, 0);

        TestUtil.runSync(() -> BlockStorage.placeBlock(multiblockLocation, Blocks.SIMPLE_MULTIBLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertMultiblockFormed(multiblockLocation, false);

        TestUtil.runSync(() -> BlockStorage.placeBlock(component1Location, Blocks.SIMPLE_BLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertMultiblockFormed(multiblockLocation, false);

        TestUtil.runSync(() -> BlockStorage.placeBlock(component2Location, Blocks.SIMPLE_BLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertMultiblockFormed(multiblockLocation, true);

        TestUtil.runSync(() -> BlockStorage.breakBlock(component2Location)).join();
        TestUtil.sleepTicks(3).join();
        assertMultiblockFormed(multiblockLocation, false);

        TestUtil.runSync(() -> BlockStorage.placeBlock(component2Location, Blocks.SIMPLE_BLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertMultiblockFormed(multiblockLocation, true);

        TestUtil.runSync(() -> {
            BlockStorage.breakBlock(multiblockLocation);
            BlockStorage.placeBlock(multiblockLocation, Blocks.SIMPLE_MULTIBLOCK);
        }).join();
        TestUtil.sleepTicks(3).join();
        assertMultiblockFormed(multiblockLocation, true);

        chunk.setForceLoaded(false);
    }
}
