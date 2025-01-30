package io.github.pylonmc.pylon.test.gametest;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.persistence.BlockStorage;
import io.github.pylonmc.pylon.core.persistence.PylonDataReader;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import static org.assertj.core.api.Assertions.assertThat;


public class BlockStorageSimpleTest {
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
            TestAddon.key("block_storage_simple_test"),
            Material.AMETHYST_BLOCK,
            TestBlock.class,
            12
    );

    public static @NotNull GameTestConfig get() {
        return new GameTestConfig.Builder(new NamespacedKey(TestAddon.instance(), "block_storage_simple_test"))
                .size(1)
                .setUp((test) -> {
                    schema.register();

                    BlockStorage.set(test.location(), schema);

                    PylonBlock<PylonBlockSchema> pylonBlock = BlockStorage.get(test.location());

                    assertThat(pylonBlock)
                            .isNotNull()
                            .isInstanceOf(TestBlock.class);

                    assertThat(pylonBlock.getSchema())
                            .isInstanceOf(TestBlockSchema.class);

                    assertThat(BlockStorage.getAs(TestBlock.class, test.location()))
                            .isNotNull();
                })
                .build();
    }
}
