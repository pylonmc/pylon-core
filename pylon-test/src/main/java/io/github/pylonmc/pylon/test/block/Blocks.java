package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Material;


public final class Blocks {

    private Blocks() {}

    public static final PylonBlockSchema SIMPLE_BLOCK = PylonBlockSchema.simple(
            PylonTest.key("simple_block"),
            Material.AMETHYST_BLOCK,
            PylonBlock::new
    );

    public static final BlockWithCustomSchema.TestSchema BLOCK_WITH_CUSTOM_SCHEMA = new BlockWithCustomSchema.TestSchema(
            PylonTest.key("block_with_custom_schema"),
            Material.AMETHYST_BLOCK,
            12
    );

    public static final PylonBlockSchema BLOCK_WITH_FIELD = PylonBlockSchema.simple(
            PylonTest.key("block_with_field"),
            Material.AMETHYST_BLOCK,
            BlockWithField::new,
            BlockWithField::new
    );

    public static final PylonBlockSchema SIMPLE_MULTIBLOCK = PylonBlockSchema.simple(
            PylonTest.key("simple_multiblock"),
            Material.AMETHYST_BLOCK,
            TestPylonSimpleMultiblock::new,
            TestPylonSimpleMultiblock::new
    );

    public static final PylonBlockSchema TICKING_ERROR_BLOCK = PylonBlockSchema.simple(
            PylonTest.key("ticking_error_block"),
            Material.AMETHYST_BLOCK,
            TickingErrorBlock::new
    );

    public static final PylonBlockSchema TICKING_BLOCK = PylonBlockSchema.simple(
            PylonTest.key("ticking_block"),
            Material.AMETHYST_BLOCK,
            TickingBlock::new
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
