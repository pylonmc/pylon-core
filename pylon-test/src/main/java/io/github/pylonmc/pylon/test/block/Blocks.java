package io.github.pylonmc.pylon.test.block;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.block.fluid.FluidConnector;
import io.github.pylonmc.pylon.test.block.fluid.FluidLimiter;
import io.github.pylonmc.pylon.test.block.fluid.consumer.LavaConsumer;
import io.github.pylonmc.pylon.test.block.fluid.consumer.WaterConsumer;
import io.github.pylonmc.pylon.test.block.fluid.producer.LavaProducer;
import io.github.pylonmc.pylon.test.block.fluid.producer.WaterProducer;
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
        PylonBlock.register(LavaConsumer.KEY, Material.AMETHYST_BLOCK, LavaConsumer.class);
        PylonBlock.register(WaterConsumer.KEY, Material.AMETHYST_BLOCK, WaterConsumer.class);
        PylonBlock.register(LavaProducer.KEY, Material.AMETHYST_BLOCK, LavaProducer.class);
        PylonBlock.register(WaterProducer.KEY, Material.AMETHYST_BLOCK, WaterProducer.class);
        PylonBlock.register(FluidLimiter.KEY, Material.AMETHYST_BLOCK, FluidLimiter.class);
        PylonBlock.register(FluidConnector.KEY, Material.AMETHYST_BLOCK, FluidConnector.class);
    }
}
