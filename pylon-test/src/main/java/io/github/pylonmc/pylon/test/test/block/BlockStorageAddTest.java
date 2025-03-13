package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNullByDefault;

import static org.assertj.core.api.Assertions.assertThat;

@NotNullByDefault
public class BlockStorageAddTest extends GameTest {
    public static class TestBlockSchema extends PylonBlockSchema {
        private final int processingSpeed;

        public TestBlockSchema(
                NamespacedKey key,
                Class<? extends PylonBlock<? extends PylonBlockSchema>> blockClass,
                int processingSpeed
        ) {
            super(key, blockClass);
            this.processingSpeed = processingSpeed;
        }
    }

    public static class TestBlock extends PylonBlock<TestBlockSchema> {
        public TestBlock(TestBlockSchema schema, Block block, BlockCreateContext context) {
            super(schema, block);
        }

        public TestBlock(
                TestBlockSchema schema,
                Block block,
                PersistentDataContainer pdc
        ) {
            super(schema, block);
        }
    }

    private static final TestBlockSchema schema = new TestBlockSchema (
            PylonTest.key("block_storage_add_test"),
            TestBlock.class,
            12
    );

    public BlockStorageAddTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "block_storage_add_test"))
                .size(1)
                .setUp((test) -> {
                    schema.register();

                    BlockStorage.placeBlock(test.location(), schema);

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
