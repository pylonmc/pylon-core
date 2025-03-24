package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.*;
import io.github.pylonmc.pylon.core.block.context.BlockContext;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.context.BlockLoadContext;
import io.github.pylonmc.pylon.core.item.PylonItemSchema;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNullByDefault;

import static org.assertj.core.api.Assertions.assertThat;

@NotNullByDefault
public class BlockStorageAddTest extends GameTest {
    public static class TestBlockSchema extends PylonBlockSchema {
        private final int processingSpeed;

        public TestBlockSchema(
                NamespacedKey key,
                Material material,
                int processingSpeed
        ) {
            super(key, material, TestBlock::new, TestBlock::new);
            this.processingSpeed = processingSpeed;
        }
    }

    public static class TestBlock extends PylonBlock<PylonBlockSchema> {
        public TestBlock(PylonBlockSchema schema, BlockCreateContext context) {
            super(schema, context);
        }

        public TestBlock(TestBlockSchema schema, BlockLoadContext context) {
            super(schema, context);
        }
    }

    private static final TestBlockSchema schema = new TestBlockSchema (
            PylonTest.key("block_storage_add_test"),
            Material.AMETHYST_BLOCK,
            12
    );

    public BlockStorageAddTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "block_storage_add_test"))
                .size(1)
                .setUp((test) -> {
                    schema.register();

                    BlockStorage.placeBlock(schema, new BlockCreateContext.PluginPlace(test.location().getBlock()));

                    PylonBlock<?> pylonBlock = BlockStorage.get(test.location());

                    assertThat(pylonBlock)
                            .isNotNull()
                            .isInstanceOf(TestBlock.class);

                    assertThat(pylonBlock.getSchema())
                            .isInstanceOf(TestBlockSchema.class)
                            .extracting(s -> (TestBlockSchema) s)
                            .extracting(s -> s.processingSpeed)
                            .isEqualTo(12);

                    assertThat(BlockStorage.getAs(TestBlock.class, test.location()))
                            .isNotNull();
                })
                .build());
    }
}
