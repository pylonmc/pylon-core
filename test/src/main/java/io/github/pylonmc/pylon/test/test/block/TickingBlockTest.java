package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.gametest.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.block.TickingBlock;


public class TickingBlockTest extends GameTest {

    public TickingBlockTest() {
        super(new GameTestConfig.Builder(PylonTest.key("ticking_block"))
                .size(1)
                .setUp((test) -> {
                    BlockStorage.placeBlock(test.location(), TickingBlock.KEY);

                    test.succeedWhen(() -> BlockStorage.getAs(TickingBlock.class, test.location()).ticks >= 5);
                })
                .build());
    }
}
