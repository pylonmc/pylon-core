package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.persistence.pdc.PylonDataReader;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import static org.assertj.core.api.Assertions.assertThat;


public class BlockStorageSimpleTest extends GameTest {
    public static class TestBlockSchema extends PylonBlockSchema {
        private final int processingSpeed;

        public TestBlockSchema(
                @NotNull NamespacedKey key,
                @NotNull Material material,
                @NotNull Class<? extends PylonBlock<? extends PylonBlockSchema>> blockClass,
                int processingSpeed
        ) {
            super(key, material, blockClass);
            this.processingSpeed = processingSpeed;
        }

        public int getProcessingSpeed() {
            return processingSpeed;
        }
    }

    public static class TestBlock extends PylonBlock<TestBlockSchema> {
        public TestBlock(@NotNull TestBlockSchema schema, @NotNull Block block) {
            super(schema, block);
        }

        public TestBlock(@NotNull PylonDataReader reader, @NotNull Block block) {
            super(reader, block);
        }
    }

    private static final TestBlockSchema schema = new TestBlockSchema (
            PylonTest.key("block_storage_simple_test"),
            Material.AMETHYST_BLOCK,
            TestBlock.class,
            12
    );

    public BlockStorageSimpleTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "block_storage_simple_test"))
                .size(1)
                .setUp((test) -> {
                    schema.register();

                    BlockStorage.set(test.location(), schema);

                    PylonBlock<PylonBlockSchema> pylonBlock = BlockStorage.get(test.location());

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
