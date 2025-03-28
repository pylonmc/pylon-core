package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.SimplePylonBlock;
import io.github.pylonmc.pylon.core.block.base.SimpleMultiblock;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class MultiblockTest extends GameTest {

    public static class TestMultiblock extends PylonBlock<PylonBlockSchema> implements SimpleMultiblock {
        private boolean formed;

        public TestMultiblock(PylonBlockSchema schema, Block block, BlockCreateContext context) {
            super(schema, block);
        }

        public TestMultiblock(
                PylonBlockSchema schema,
                Block block,
                PersistentDataContainer pdc
        ) {
            super(schema, block);
        }

        @Override
        public boolean getFormed() {
            return formed;
        }

        @Override
        public void setFormed(boolean formed) {
            this.formed = formed;
        }

        @Override
        public @NotNull Block getCenter() {
            return getBlock().getRelative(BlockFace.UP);
        }

        @Override
        public @NotNull Map<Vector, Component> getComponents() {
            return Map.of(
                    new Vector(1, 0, 0), new SimpleMultiblock.PylonComponent(TEST_COMPONENT.getKey())
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


    public MultiblockTest() {
        super(new GameTestConfig.Builder(PylonTest.key("simple_multiblock"))
                .size(3)
                .timeoutTicks(30 * 20)
                .setUp((test) -> {
                    TEST_COMPONENT.register();
                    TEST_MULTIBLOCK.register();

                    Location multiblockLocation = test.location();
                    Location componentLocation = test.location().add(1, 1, 0);

                    BlockStorage.placeBlock(multiblockLocation, TEST_MULTIBLOCK);
                    assertThat(BlockStorage.get(multiblockLocation))
                            .isInstanceOfSatisfying(TestMultiblock.class, block ->
                                    assertThat(block.formed).isFalse());

                    BlockStorage.placeBlock(componentLocation, TEST_COMPONENT);
                    assertThat(BlockStorage.get(multiblockLocation))
                            .isInstanceOfSatisfying(TestMultiblock.class, block ->
                                    assertThat(block.formed).isTrue());
                })
                .build());
    }
}
