package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.SimplePylonBlock;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.fluid.Fluids;
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
            TestPylonSimpleMultiblock.class
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

    public static final PylonBlockSchema WATER_CONSUMER = new FluidConsumer.Schema(
            PylonTest.key("water_consumer"),
            Material.AMETHYST_BLOCK,
            Fluids.WATER,
            100
    );

    public static final PylonBlockSchema LAVA_CONSUMER = new FluidConsumer.Schema(
            PylonTest.key("lava_consumer"),
            Material.AMETHYST_BLOCK,
            Fluids.LAVA,
            100
    );

    public static final PylonBlockSchema WATER_PRODUCER = new FluidProducer.Schema(
            PylonTest.key("water_producer"),
            Material.AMETHYST_BLOCK,
            Fluids.WATER
    );

    public static final PylonBlockSchema LAVA_PRODUCER = new FluidProducer.Schema(
            PylonTest.key("lava_producer"),
            Material.AMETHYST_BLOCK,
            Fluids.LAVA
    );

    public static final FluidLimiter.Schema FLUID_LIMITER = new FluidLimiter.Schema(
            PylonTest.key("fluid_limiter"),
            Material.AMETHYST_BLOCK,
            50
    );

    public static final PylonBlockSchema FLUID_CONNECTOR = new PylonBlockSchema(
            PylonTest.key("fluid_connector"),
            Material.AMETHYST_BLOCK,
            FluidConnector.class
    );

    public static void register() {
        SIMPLE_BLOCK.register();
        BLOCK_WITH_CUSTOM_SCHEMA.register();
        BLOCK_WITH_FIELD.register();
        SIMPLE_MULTIBLOCK.register();
        TICKING_ERROR_BLOCK.register();
        TICKING_BLOCK.register();
        WATER_CONSUMER.register();
        LAVA_CONSUMER.register();
        WATER_PRODUCER.register();
        LAVA_PRODUCER.register();
        FLUID_LIMITER.register();
        FLUID_CONNECTOR.register();
    }
}
