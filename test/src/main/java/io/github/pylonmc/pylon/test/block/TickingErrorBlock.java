package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.rebar.block.PylonBlock;
import io.github.pylonmc.rebar.block.base.PylonTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;


public class TickingErrorBlock extends PylonBlock implements PylonTickingBlock {

    public static final NamespacedKey KEY = PylonTest.key("ticking_error_block");

    @SuppressWarnings("unused")
    public TickingErrorBlock(Block block, BlockCreateContext context) {
        super(block);
    }

    @SuppressWarnings("unused")
    public TickingErrorBlock(Block block, PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public void tick() {
        throw new RuntimeException("This exception is thrown as part of a test");
    }
}