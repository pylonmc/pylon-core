package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.block.TestPylonSimpleMultiblock;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;

import static org.assertj.core.api.Assertions.assertThat;


public class SimpleMultiblockTest extends AsyncTest {

    public static void assertMultiblockFormed(Location multiblockLocation, boolean formed) {
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(TestPylonSimpleMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isEqualTo(formed)
                );
    }

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Location multiblockLocation = chunk.getBlock(5, 100, 5).getLocation();
        Location component1Location = multiblockLocation.clone().add(1, 1, 4);
        Location component2Location = multiblockLocation.clone().add(2, -1, 0);

        TestUtil.runSync(() -> BlockStorage.placeBlock(multiblockLocation, TestPylonSimpleMultiblock.KEY)).join();
        TestUtil.sleepTicks(2).join();
        assertMultiblockFormed(multiblockLocation, false);

        TestUtil.runSync(() -> BlockStorage.placeBlock(component1Location, Blocks.SIMPLE_BLOCK_KEY)).join();
        TestUtil.sleepTicks(2).join();
        assertMultiblockFormed(multiblockLocation, false);

        TestUtil.runSync(() -> BlockStorage.placeBlock(component2Location, Blocks.SIMPLE_BLOCK_KEY)).join();
        TestUtil.sleepTicks(2).join();
        assertMultiblockFormed(multiblockLocation, true);

        TestUtil.runSync(() -> BlockStorage.breakBlock(component2Location)).join();
        TestUtil.sleepTicks(2).join();
        assertMultiblockFormed(multiblockLocation, false);

        chunk.setForceLoaded(false);
        TestUtil.unloadChunk(chunk).join();
        TestUtil.loadChunk(chunk).join();
        chunk.setForceLoaded(true);

        TestUtil.runSync(() -> BlockStorage.placeBlock(component2Location, Blocks.SIMPLE_BLOCK_KEY)).join();
        TestUtil.sleepTicks(2).join();
        assertMultiblockFormed(multiblockLocation, true);

        TestUtil.runSync(() -> {
            BlockStorage.breakBlock(multiblockLocation);
            BlockStorage.placeBlock(multiblockLocation, TestPylonSimpleMultiblock.KEY);
        }).join();
        TestUtil.sleepTicks(2).join();
        assertMultiblockFormed(multiblockLocation, true);

        chunk.setForceLoaded(false);
    }
}
