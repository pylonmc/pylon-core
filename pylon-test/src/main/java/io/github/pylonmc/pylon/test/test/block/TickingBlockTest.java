package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.Ticking;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;

public class TickingBlockTest extends GameTest {

    public static class TestBlockSchema extends PylonBlockSchema {
        public TestBlockSchema(
                NamespacedKey key,
                Material material,
                Class<? extends PylonBlock<? extends PylonBlockSchema>> blockClass
        ) {
            super(key, material, blockClass);
        }
    }

    public static class TestBlock extends PylonBlock<TestBlockSchema> implements Ticking {

        public static int ticks = 0;

        public TestBlock(TestBlockSchema schema, Block block, BlockCreateContext context) {
            super(schema, block);
        }

        public TestBlock(TestBlockSchema schema, Block block, PersistentDataContainer pdc) {
            super(schema, block);
        }

        @Override
        public void tick(double deltaSeconds) {
            ticks++;
        }
    }

    private static final TestBlockSchema schema = new TestBlockSchema(
            PylonTest.key("ticking_block"),
            Material.AMETHYST_BLOCK,
            TestBlock.class
    );

    public TickingBlockTest() {
        super(new GameTestConfig.Builder(PylonTest.key("ticking_block"))
                .size(1)
                .setUp((test) -> {
                    schema.register();

                    TestBlock.ticks = 0;
                    BlockStorage.set(test.location(), schema);

                    test.succeedWhen(() -> TestBlock.ticks >= 5);
                })
                .build());
    }
}
