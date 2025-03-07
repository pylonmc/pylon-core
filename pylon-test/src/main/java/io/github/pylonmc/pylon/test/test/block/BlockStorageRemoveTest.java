package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.SimplePylonBlock;
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageRemoveTest extends GameTest {
    private static final PylonBlockSchema schema = new PylonBlockSchema(
            PylonTest.key("block_storage_remove_test"),
            Material.AMETHYST_BLOCK,
            SimplePylonBlock.class
    );

    public BlockStorageRemoveTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "block_storage_remove_test"))
                .size(1)
                .setUp((test) -> {
                    schema.register();

                    BlockStorage.placeBlock(test.location(), schema);

                    assertThat(BlockStorage.get(test.location()))
                            .isNotNull();

                    BlockStorage.breakBlock(test.location());

                    assertThat(BlockStorage.get(test.location()))
                            .isNull();
                })
                .build());
    }
}
