package io.github.pylonmc.rebar.test.test.block;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.gametest.GameTestConfig;
import io.github.pylonmc.rebar.test.RebarTest;
import io.github.pylonmc.rebar.test.base.GameTest;
import io.github.pylonmc.rebar.test.block.TickingErrorBlock;


public class TickingBlockErrorTest extends GameTest {

    public TickingBlockErrorTest() {
        super(new GameTestConfig.Builder(RebarTest.key("ticking_error_block"))
                .size(1)
                .setUp((test) -> {
                    BlockStorage.placeBlock(test.location(), TickingErrorBlock.KEY);

                    test.succeedWhen(() -> !RebarTickingBlock.isTicking(BlockStorage.get(test.location().getBlock())));
                })
                .build());
    }
}
