package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.base.PylonTickingBlock;
import io.github.pylonmc.rebar.gametest.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.block.TickingErrorBlock;


public class TickingBlockErrorTest extends GameTest {

    public TickingBlockErrorTest() {
        super(new GameTestConfig.Builder(PylonTest.key("ticking_error_block"))
                .size(1)
                .setUp((test) -> {
                    BlockStorage.placeBlock(test.location(), TickingErrorBlock.KEY);

                    test.succeedWhen(() -> !PylonTickingBlock.isTicking(BlockStorage.get(test.location().getBlock())));
                })
                .build());
    }
}
