package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.block.PhantomBlock;
import io.github.pylonmc.pylon.core.config.PylonConfig;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.block.BlockEventError;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.block.BellRingEvent;

public class BlockEventErrorTest extends GameTest {
    public BlockEventErrorTest(){
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "block_event_error_test"))
                .size(1)
                .setUp(test -> {
                    Block block = BlockStorage.placeBlock(test.location(), BlockEventError.KEY).getBlock();
                    Entity theRinger = test.location().getWorld().spawn(test.location().clone().add(1, 0, 0), Skeleton.class);
                    for(int i = 0; i < PylonConfig.getAllowedBlockErrors(); i++){
                        new BellRingEvent(block, BlockFace.EAST, theRinger);
                    }
                    test.succeedWhen(() -> BlockStorage.get(block) instanceof PhantomBlock);
                })
                .build()
        );
    }
}
