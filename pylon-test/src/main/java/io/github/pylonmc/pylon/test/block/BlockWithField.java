package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.test.PylonTest;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;


public class BlockWithField extends PylonBlock<PylonBlockSchema> {

    private static final NamespacedKey PROGRESS_KEY = PylonTest.key("block_with_field_progress");

    @Getter
    private final int progress;

    public BlockWithField(PylonBlockSchema testSchema, Block block) {
        super(testSchema, block);
        progress = 240;
    }

    @SuppressWarnings("DataFlowIssue")
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
}