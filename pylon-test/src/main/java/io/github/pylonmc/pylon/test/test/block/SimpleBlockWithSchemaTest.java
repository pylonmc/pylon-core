package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;

import static org.assertj.core.api.Assertions.assertThat;


public class SimpleBlockWithSchemaTest extends SyncTest {
    public static class TestBlockSchema extends PylonBlockSchema {
        private final int processingSpeed;

        public TestBlockSchema(
                NamespacedKey key,
                Material material,
                Class<? extends PylonBlock<? extends PylonBlockSchema>> blockClass,
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
        public TestBlock(TestBlockSchema schema, Block block, BlockCreateContext context) {
            super(schema, block);
        }

        public TestBlock(
                TestBlockSchema schema,
                Block block,
                PersistentDataContainer pdc) {
            super(schema, block);
        }
    }

    @Override
    public void test() {
        NamespacedKey key = PylonTest.key("simple_block_with_schema_test");
        new TestBlockSchema(
                key,
                Material.AMETHYST_BLOCK,
                TestBlock.class,
                12
        ).register();

        assertThat(PylonRegistry.BLOCKS.get(key))
                .isInstanceOf(TestBlockSchema.class)
                .extracting(schema -> (TestBlockSchema) schema)
                .extracting(TestBlockSchema::getProcessingSpeed)
                .isEqualTo(12);
    }
}
