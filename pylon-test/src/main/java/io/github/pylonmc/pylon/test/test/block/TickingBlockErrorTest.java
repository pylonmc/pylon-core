package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.TickManager;
import io.github.pylonmc.pylon.core.block.base.Ticking;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;

public class TickingBlockErrorTest extends GameTest {

    public static class TestBlockSchema extends PylonBlockSchema {
        public TestBlockSchema(
                NamespacedKey key,
                Class<? extends PylonBlock<? extends PylonBlockSchema>> blockClass
        ) {
            super(key, blockClass);
        }
    }

    public static class TestBlock extends PylonBlock<TestBlockSchema> implements Ticking {

        public TestBlock(TestBlockSchema schema, Block block, BlockCreateContext context) {
            super(schema, block);
        }

        public TestBlock(TestBlockSchema schema, Block block, PersistentDataContainer pdc) {
            super(schema, block);
        }

        @Override
        public void tick(double deltaSeconds) {
            throw new RuntimeException();
        }
    }

    private static final TestBlockSchema schema = new TestBlockSchema(
            PylonTest.key("ticking_error_block"),
            TestBlock.class
    );

    public TickingBlockErrorTest() {
        super(new GameTestConfig.Builder(PylonTest.key("ticking_error_block"))
                .size(1)
                .setUp((test) -> {
                    schema.register();

                    BlockStorage.placeBlock(test.location(), schema);

                    test.succeedWhen(() -> !TickManager.isTicking(BlockStorage.get(test.location())));
                })
                .build());
    }
}
