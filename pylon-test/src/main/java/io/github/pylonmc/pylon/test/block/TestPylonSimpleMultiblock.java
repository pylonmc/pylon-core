package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.base.PylonSimpleMultiblock;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.Map;


public class TestPylonSimpleMultiblock extends PylonBlock<PylonBlockSchema> implements PylonSimpleMultiblock {

    @SuppressWarnings("unused")
    public TestPylonSimpleMultiblock(PylonBlockSchema schema, Block block, BlockCreateContext context) {
        super(schema, block);
    }

    @SuppressWarnings("unused")
    public TestPylonSimpleMultiblock(
            PylonBlockSchema schema,
            Block block,
            PersistentDataContainer pdc
    ) {
        super(schema, block);
    }

    @Override
    public @NotNull Map<Vector3i, Component> getComponents() {
        return Map.of(
                new Vector3i(1, 1, 4), new PylonComponent(Blocks.SIMPLE_BLOCK.getKey()),
                new Vector3i(2, -1, 0), new PylonComponent(Blocks.SIMPLE_BLOCK.getKey())
        );
    }
}