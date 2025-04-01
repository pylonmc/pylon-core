package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.base.Ticking;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;


public class TickingErrorBlock extends PylonBlock<PylonBlockSchema> implements Ticking {

    @SuppressWarnings("unused")
    public TickingErrorBlock(PylonBlockSchema schema, Block block, BlockCreateContext context) {
        super(schema, block);
    }

    @SuppressWarnings("unused")
    public TickingErrorBlock(PylonBlockSchema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block);
    }

    @Override
    public void tick(double deltaSeconds) {
        throw new RuntimeException("This exception is thrown as part of a test");
    }
}