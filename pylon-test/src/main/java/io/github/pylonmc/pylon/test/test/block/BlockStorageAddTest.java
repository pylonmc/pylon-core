package io.github.pylonmc.pylon.test.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.base.GameTest;
import io.github.pylonmc.pylon.test.block.BlockWithCustomSchema;
import io.github.pylonmc.pylon.test.block.Blocks;
import org.bukkit.NamespacedKey;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockStorageAddTest extends GameTest {

    public BlockStorageAddTest() {
        super(new GameTestConfig.Builder(new NamespacedKey(PylonTest.instance(), "block_storage_add_test"))
                .size(1)
                .setUp((test) -> {
                    BlockStorage.placeBlock(test.location(), Blocks.BLOCK_WITH_CUSTOM_SCHEMA);

                    PylonBlock<?> pylonBlock = BlockStorage.get(test.location());

                    assertThat(pylonBlock)
                            .isNotNull()
                            .isInstanceOf(BlockWithCustomSchema.TestBlock.class);

                    //noinspection Convert2MethodRef
                    assertThat(pylonBlock.getSchema())
                            .isInstanceOf(BlockWithCustomSchema.TestSchema.class)
                            .extracting(s -> (BlockWithCustomSchema.TestSchema) s)
                            .extracting(s -> s.getProcessingSpeed())
                            .isEqualTo(12);

                    assertThat(BlockStorage.getAs(BlockWithCustomSchema.TestBlock.class, test.location()))
                            .isNotNull();
                })
                .build());
    }
}
