package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.block.TickManager;
import io.github.pylonmc.pylon.core.gametest.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.block.TickingErrorBlock;


public class TickingBlockErrorTest extends GameTest {

    public TickingBlockErrorTest() {
        super(new GameTestConfig.Builder(PylonTest.key("ticking_error_block"))
                .size(1)
                .setUp((test) -> {
                    BlockStorage.placeBlock(test.location(), TickingErrorBlock.KEY);

                    test.succeedWhen(() -> !TickManager.isTicking(BlockStorage.get(test.location())));
                })
                .build());
    }
}
