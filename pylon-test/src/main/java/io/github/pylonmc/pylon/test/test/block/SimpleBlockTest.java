package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.SimplePylonBlock;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;

public class SimpleBlockTest extends SyncTest {
    @Override
    public void test() {
        new PylonBlockSchema(
                PylonTest.key("simple_block_test"),
                SimplePylonBlock.class
        ).register();
    }
}
