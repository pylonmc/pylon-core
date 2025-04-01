package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.SimplePylonBlock;
import io.github.pylonmc.pylon.core.block.base.SimpleMultiblock;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.util.TestUtil;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class MultiblockTest extends AsyncTest {

    public static class TestMultiblock extends PylonBlock<PylonBlockSchema> implements SimpleMultiblock {

        @SuppressWarnings("unused")
        public TestMultiblock(PylonBlockSchema schema, Block block, BlockCreateContext context) {
            super(schema, block);
        }

        @SuppressWarnings("unused")
        public TestMultiblock(
                PylonBlockSchema schema,
                Block block,
                PersistentDataContainer pdc
        ) {
            super(schema, block);
        }

        @Override
        public @NotNull Map<Vector3i, Component> getComponents() {
            return Map.of(
                    new Vector3i(1, 1, 0), new SimpleMultiblock.PylonComponent(TEST_COMPONENT.getKey()),
                    new Vector3i(2, -1, 0), new SimpleMultiblock.PylonComponent(TEST_COMPONENT.getKey())
            );
        }
    }

    private static final PylonBlockSchema TEST_COMPONENT = new PylonBlockSchema(
            PylonTest.key("multiblock_test_component"),
            Material.WAXED_EXPOSED_CHISELED_COPPER,
            SimplePylonBlock.class
    );

    private static final PylonBlockSchema TEST_MULTIBLOCK = new PylonBlockSchema(
            PylonTest.key("multiblock_test_multiblock"),
            Material.AMETHYST_BLOCK,
            TestMultiblock.class
    );

    @Override
    protected void test() {
        TEST_COMPONENT.register();
        TEST_MULTIBLOCK.register();

        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Location multiblockLocation = chunk.getBlock(4, 100, 4).getLocation();
        Location component1Location = multiblockLocation.clone().add(1, 1, 0);
        Location component2Location = multiblockLocation.clone().add(2, -1, 0);

        TestUtil.runSync(() -> BlockStorage.placeBlock(multiblockLocation, TEST_MULTIBLOCK)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(TestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isFalse());

        TestUtil.runSync(() -> BlockStorage.placeBlock(component1Location, TEST_COMPONENT)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(TestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isFalse());

        TestUtil.runSync(() -> BlockStorage.placeBlock(component2Location, TEST_COMPONENT)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(TestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isTrue());

        TestUtil.runSync(() -> BlockStorage.breakBlock(component2Location)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(TestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isFalse());

        TestUtil.runSync(() -> BlockStorage.placeBlock(component2Location, TEST_COMPONENT)).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(TestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isTrue());

        TestUtil.runSync(() -> {
            BlockStorage.breakBlock(multiblockLocation);
            BlockStorage.placeBlock(multiblockLocation, TEST_MULTIBLOCK);
        }).join();
        TestUtil.sleepTicks(3).join();
        assertThat(BlockStorage.get(multiblockLocation))
                .isInstanceOfSatisfying(TestMultiblock.class, block ->
                        assertThat(block.isFormedAndFullyLoaded()).isTrue());

        chunk.setForceLoaded(false);
    }
}
