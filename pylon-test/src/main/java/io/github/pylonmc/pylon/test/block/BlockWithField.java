package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;


public class BlockWithField extends PylonBlock<PylonBlockSchema> {

    private static final NamespacedKey PROGRESS_KEY = PylonTest.key("block_with_field_progress");

    private final int progress;

    @SuppressWarnings("unused")
    public BlockWithField(PylonBlockSchema testSchema, Block block, BlockCreateContext context) {
        super(testSchema, block);
        progress = 240;
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public BlockWithField(
            PylonBlockSchema testSchema,
            Block block,
            PersistentDataContainer pdc
    ) {
        super(testSchema, block);
        progress = pdc.get(PROGRESS_KEY, PersistentDataType.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(PROGRESS_KEY, PersistentDataType.INTEGER, 130);
    }

    public int getProgress() {
        return progress;
    }
}