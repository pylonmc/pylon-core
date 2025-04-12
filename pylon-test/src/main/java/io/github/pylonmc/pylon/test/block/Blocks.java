package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.SimplePylonBlock;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Material;


public final class Blocks {

    private Blocks() {}

    public static final PylonBlockSchema SIMPLE_BLOCK = new PylonBlockSchema(
            PylonTest.key("simple_block"),
            Material.AMETHYST_BLOCK,
            SimplePylonBlock.class
    );

    public static final BlockWithCustomSchema.TestSchema BLOCK_WITH_CUSTOM_SCHEMA = new BlockWithCustomSchema.TestSchema(
            PylonTest.key("block_with_custom_schema"),
            Material.AMETHYST_BLOCK,
            BlockWithCustomSchema.TestBlock.class,
            12
    );

    public static final PylonBlockSchema BLOCK_WITH_FIELD = new PylonBlockSchema(
            PylonTest.key("block_with_field"),
            Material.AMETHYST_BLOCK,
            BlockWithField.class
    );

    public static final PylonBlockSchema SIMPLE_MULTIBLOCK = new PylonBlockSchema(
            PylonTest.key("simple_multiblock"),
            Material.AMETHYST_BLOCK,
            SimpleTestPylonMultiblock.class
    );

    public static final PylonBlockSchema TICKING_ERROR_BLOCK = new PylonBlockSchema(
            PylonTest.key("ticking_error_block"),
            Material.AMETHYST_BLOCK,
            TickingErrorBlock.class
    );

    public static final PylonBlockSchema TICKING_BLOCK = new PylonBlockSchema(
            PylonTest.key("ticking_block"),
            Material.AMETHYST_BLOCK,
            TickingBlock.class
    );

    public static void register() {
        SIMPLE_BLOCK.register();
        BLOCK_WITH_CUSTOM_SCHEMA.register();
        BLOCK_WITH_FIELD.register();
        SIMPLE_MULTIBLOCK.register();
        TICKING_ERROR_BLOCK.register();
        TICKING_BLOCK.register();
    }
}
