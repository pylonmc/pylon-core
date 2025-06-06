package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.base.PylonSimpleMultiblock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class TestPylonSimpleMultiblock extends PylonBlock implements PylonSimpleMultiblock {

    public static final NamespacedKey KEY = PylonTest.key("simple_multiblock");

    private final Map<String, UUID> heldEntities;

    @SuppressWarnings("unused")
    public TestPylonSimpleMultiblock(Block block, BlockCreateContext context) {
        super(block);
        heldEntities = new HashMap<>();
    }

    @SuppressWarnings("unused")
    public TestPylonSimpleMultiblock(Block block, PersistentDataContainer pdc) {
        super(block);
        heldEntities = loadHeldEntities(pdc);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        saveHeldEntities(pdc);
    }

    @Override
    public @NotNull Map<Vector3i, Component> getComponents() {
        return Map.of(
                new Vector3i(1, 1, 4), new PylonComponent(Blocks.SIMPLE_BLOCK_KEY),
                new Vector3i(2, -1, 0), new PylonComponent(Blocks.SIMPLE_BLOCK_KEY)
        );
    }

    @Override
    public @NotNull Map<String, UUID> getHeldEntities() {
        return heldEntities;
    }
}