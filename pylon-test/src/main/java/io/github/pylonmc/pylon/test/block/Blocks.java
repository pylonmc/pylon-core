package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.block.fluid.FluidConnector;
import io.github.pylonmc.pylon.test.block.fluid.FluidConsumer;
import io.github.pylonmc.pylon.test.block.fluid.FluidLimiter;
import io.github.pylonmc.pylon.test.block.fluid.FluidProducer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;


public final class Blocks {

    private Blocks() {}

    public static final NamespacedKey SIMPLE_BLOCK_KEY = PylonTest.key("simple_block");

    public static void register() {
        PylonBlock.register(SIMPLE_BLOCK_KEY, Material.AMETHYST_BLOCK, PylonBlock.class);
        PylonBlock.register(BlockWithField.KEY, Material.AMETHYST_BLOCK, BlockWithField.class);
        PylonBlock.register(TestPylonSimpleMultiblock.KEY, Material.AMETHYST_BLOCK, TestPylonSimpleMultiblock.class);
        PylonBlock.register(TickingErrorBlock.KEY, Material.AMETHYST_BLOCK, TickingErrorBlock.class);
        PylonBlock.register(TickingBlock.KEY, Material.AMETHYST_BLOCK, TickingBlock.class);
        PylonBlock.register(FluidConsumer.LAVA_CONSUMER_KEY, Material.AMETHYST_BLOCK, FluidConsumer.class);
        PylonBlock.register(FluidConsumer.WATER_CONSUMER_KEY, Material.AMETHYST_BLOCK, FluidConsumer.class);
        PylonBlock.register(FluidProducer.LAVA_PRODUCER_KEY, Material.AMETHYST_BLOCK, FluidProducer.class);
        PylonBlock.register(FluidProducer.WATER_PRODUCER_KEY, Material.AMETHYST_BLOCK, FluidProducer.class);
        PylonBlock.register(FluidLimiter.KEY, Material.AMETHYST_BLOCK, FluidLimiter.class);
        PylonBlock.register(FluidConnector.KEY, Material.AMETHYST_BLOCK, FluidConnector.class);
        PylonBlock.register(BlockEventError.KEY, Material.TARGET, BlockEventError.class);
    }
}
