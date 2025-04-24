package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.base.PylonTickingBlock;
import org.bukkit.block.Block;


public class TickingErrorBlock extends PylonBlock<PylonBlockSchema> implements PylonTickingBlock {

    public TickingErrorBlock(PylonBlockSchema schema, Block block) {
        super(schema, block);
    }

    @Override
    public void tick(double deltaSeconds) {
        throw new RuntimeException("This exception is thrown as part of a test");
    }
}