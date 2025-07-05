package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.base.PylonSimpleMultiblock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.Map;


public class TestPylonSimpleMultiblock extends PylonBlock implements PylonSimpleMultiblock {

    public static final NamespacedKey KEY = PylonTest.key("simple_multiblock");

    @SuppressWarnings("unused")
    public TestPylonSimpleMultiblock(Block block, BlockCreateContext context) {
        super(block);
    }

    @SuppressWarnings("unused")
    public TestPylonSimpleMultiblock(Block block, PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        return Map.of(
                new Vector3i(1, 1, 4), new PylonMultiblockComponent(Blocks.SIMPLE_BLOCK_KEY),
                new Vector3i(2, -1, 0), new PylonMultiblockComponent(Blocks.SIMPLE_BLOCK_KEY)
        );
    }
}