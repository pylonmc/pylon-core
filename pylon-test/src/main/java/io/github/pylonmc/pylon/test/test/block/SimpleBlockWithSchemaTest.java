package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.context.BlockContext;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.context.BlockLoadContext;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import static org.assertj.core.api.Assertions.assertThat;


public class SimpleBlockWithSchemaTest extends SyncTest {
    public static class TestBlockSchema extends PylonBlockSchema {
        private final int processingSpeed;

        public TestBlockSchema(
                NamespacedKey key,
                Material material,
                int processingSpeed
        ) {
            super(key, material,
                    (TestBlockSchema schema, BlockCreateContext context) -> new TestBlock(schema, context),
                    (TestBlockSchema schema, BlockLoadContext context) -> new TestBlock(schema, context));
            this.processingSpeed = processingSpeed;
        }

        public int getProcessingSpeed() {
            return processingSpeed;
        }
    }

    public static class TestBlock extends PylonBlock<TestBlockSchema> {
        public TestBlock(TestBlockSchema schema, BlockContext context) {
            super(schema, context);
        }
    }

    @Override
    public void test() {
        NamespacedKey key = PylonTest.key("simple_block_with_schema_test");
        new TestBlockSchema(
                key,
                Material.AMETHYST_BLOCK,
                12
        ).register();

        assertThat(PylonRegistry.BLOCKS.get(key))
                .isInstanceOf(TestBlockSchema.class)
                .extracting(schema -> (TestBlockSchema) schema)
                .extracting(TestBlockSchema::getProcessingSpeed)
                .isEqualTo(12);
    }
}
