package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.test.PylonTest;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;


public class BlockWithField extends PylonBlock {

    public static final NamespacedKey KEY = PylonTest.key("block_with_field");
    public static final NamespacedKey PROGRESS_KEY = PylonTest.key("progress");

    @Getter private final int progress;

    @SuppressWarnings("unused")
    public BlockWithField(PylonBlockSchema schema, Block block, BlockCreateContext context) {
        super(schema, block);
        progress = 240;
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public BlockWithField(PylonBlockSchema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block);
        progress = pdc.get(PROGRESS_KEY, PersistentDataType.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(PROGRESS_KEY, PersistentDataType.INTEGER, 130);
    }
}