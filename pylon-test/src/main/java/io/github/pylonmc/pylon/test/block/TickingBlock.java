package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.base.PylonTickingBlock;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;


public class TickingBlock extends PylonBlock<PylonBlockSchema> implements PylonTickingBlock {

    public int ticks = 0;

    @SuppressWarnings("unused")
    public TickingBlock (PylonBlockSchema schema, Block block, BlockCreateContext context) {
        super(schema, block);
    }

    @SuppressWarnings("unused")
    public TickingBlock (PylonBlockSchema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block);
    }

    @Override
    public void tick(double deltaSeconds) {
        ticks++;
    }
}