package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.rebar.block.PylonBlock;
import io.github.pylonmc.rebar.block.base.PylonBell;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class BlockEventError extends PylonBlock implements PylonBell {
    public static final NamespacedKey KEY = new NamespacedKey(PylonTest.instance(), "block_event_error");
    public BlockEventError(Block block, BlockCreateContext context){
        super(block);
    }
    public BlockEventError(Block block, PersistentDataContainer pdc){
        super(block);
    }

    @Override
    public void onRing(@NotNull BellRingEvent event) {
        throw new RuntimeException("This exception is thrown as part of a test");
    }
}
