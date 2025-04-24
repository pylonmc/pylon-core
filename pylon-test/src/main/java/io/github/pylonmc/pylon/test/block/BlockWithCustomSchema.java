package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public final class BlockWithCustomSchema {

    private BlockWithCustomSchema() {}

    public static class TestSchema extends PylonBlockSchema {

        @Getter
        private final int processingSpeed;

        @SuppressWarnings("unused")
        public TestSchema(
                NamespacedKey key,
                Material material,
                int processingSpeed
        ) {
            super(key, material);
            this.processingSpeed = processingSpeed;
        }

        @Override
        public @NotNull PylonBlock<?> createBlock(@NotNull Block block, @NotNull BlockCreateContext context) {
            return new TestBlock(this, block);
        }

        @Override
        public @NotNull PylonBlock<?> loadBlock(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
            return new TestBlock(this, block);
        }
    }

    public static class TestBlock extends PylonBlock<TestSchema> {
        public TestBlock(TestSchema testSchema, Block block) {
            super(testSchema, block);
        }
    }
}
