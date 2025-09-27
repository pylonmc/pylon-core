package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.gametest.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.block.Blocks;
import org.bukkit.NamespacedKey;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageRemoveTest extends GameTest {

    public BlockStorageRemoveTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "block_storage_remove_test"))
                .size(1)
                .setUp((test) -> {
                    BlockStorage.placeBlock(test.location(), Blocks.SIMPLE_BLOCK_KEY);
                    assertThat(BlockStorage.get(test.location()))
                            .isNotNull();

                    BlockStorage.breakBlock(test.location());
                    assertThat(BlockStorage.get(test.location()))
                            .isNull();
                })
                .build());
    }
}
