package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.context.BlockContext;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.TickManager;
import io.github.pylonmc.pylon.core.block.base.Ticking;
import io.github.pylonmc.pylon.core.block.context.BlockLoadContext;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class TickingBlockErrorTest extends GameTest {

    public static class TestBlockSchema extends PylonBlockSchema {
        public TestBlockSchema(
                NamespacedKey key,
                Material material
        ) {
            super(key, material,
                    (TestBlockSchema schema, BlockCreateContext context) -> new TestBlock(schema, context),
                    (TestBlockSchema schema, BlockLoadContext context) -> new TestBlock(schema, context));
        }
    }

    public static class TestBlock extends PylonBlock<TestBlockSchema> implements Ticking {

        public TestBlock(TestBlockSchema schema, BlockContext context) {
            super(schema, context);
        }

        @Override
        public void tick(double deltaSeconds) {
            throw new RuntimeException();
        }
    }

    private static final TestBlockSchema schema = new TestBlockSchema(
            PylonTest.key("ticking_error_block"),
            Material.AMETHYST_BLOCK
    );

    public TickingBlockErrorTest() {
        super(new GameTestConfig.Builder(PylonTest.key("ticking_error_block"))
                .size(1)
                .setUp((test) -> {
                    schema.register();

                    BlockStorage.placeBlock(schema, new BlockCreateContext.PluginPlace(test.location().getBlock()));

                    test.succeedWhen(() -> !TickManager.isTicking(BlockStorage.get(test.location())));
                })
                .build());
    }
}
