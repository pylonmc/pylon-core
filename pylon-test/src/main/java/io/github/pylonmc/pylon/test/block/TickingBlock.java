package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.base.PylonTickingBlock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;


public class TickingBlock extends PylonBlock implements PylonTickingBlock {

    public static final NamespacedKey KEY = PylonTest.key("ticking_block");

    public int ticks = 0;

    @SuppressWarnings("unused")
    public TickingBlock (Block block, BlockCreateContext context) {
        super(block);
    }

    @SuppressWarnings("unused")
    public TickingBlock (Block block, PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public void tick(double deltaSeconds) {
        ticks++;
    }
}