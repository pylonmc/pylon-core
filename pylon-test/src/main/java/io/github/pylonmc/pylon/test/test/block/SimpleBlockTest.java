package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.persistence.PylonDataReader;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.SyncTest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;


public class SimpleBlockTest extends SyncTest {
    static class TestBlock extends PylonBlock<PylonBlockSchema> {
        public TestBlock(@NotNull PylonDataReader reader, @NotNull Block block) {
            super(reader, block);
        }
    }

    @Override
    public void test() {
        new PylonBlockSchema(
                PylonTest.key("pylon_item_stack_simple_test"),
                Material.AMETHYST_BLOCK,
                TestBlock.class
        ).register();
    }
}
