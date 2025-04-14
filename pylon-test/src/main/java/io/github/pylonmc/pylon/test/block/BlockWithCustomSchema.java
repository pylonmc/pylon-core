package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;


public final class BlockWithCustomSchema {

    private BlockWithCustomSchema() {}

    public static class TestSchema extends PylonBlockSchema {

        private final int processingSpeed;

        @SuppressWarnings("unused")
        public TestSchema(
                NamespacedKey key,
                Material material,
                Class<? extends PylonBlock<? extends PylonBlockSchema>> blockClass,
                int processingSpeed
        ) {
            super(key, material, blockClass);
            this.processingSpeed = processingSpeed;
        }

        @SuppressWarnings("unused")
        public int getProcessingSpeed() {
            return processingSpeed;
        }
    }

    public static class TestBlock extends PylonBlock<TestSchema> {

        @SuppressWarnings("unused")
        public TestBlock(TestSchema testSchema, Block block, BlockCreateContext context) {
            super(testSchema, block);
        }

        @SuppressWarnings("unused")
        public TestBlock(
                TestSchema testSchema,
                Block block,
                PersistentDataContainer pdc
        ) {
            super(testSchema, block);
        }
    }
}
