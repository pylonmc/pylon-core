package io.github.pylonmc.pylon.test.test.fluid;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.fluid.consumer.FluidConsumer;
import io.github.pylonmc.pylon.test.block.fluid.consumer.LavaConsumer;
import io.github.pylonmc.pylon.test.block.fluid.consumer.WaterConsumer;
import io.github.pylonmc.pylon.test.block.fluid.producer.FluidProducer;
import io.github.pylonmc.pylon.test.block.fluid.producer.LavaProducer;
import io.github.pylonmc.pylon.test.block.fluid.producer.WaterProducer;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;


public class FluidFlowRateTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Block waterProducerBlock = chunk.getBlock(2, 64, 5);
        FluidProducer waterProducer = (FluidProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(waterProducerBlock, WaterProducer.KEY)
        ).join();

        Block waterConsumerBlock = chunk.getBlock(6, 64, 4);
        FluidConsumer waterConsumer = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(waterConsumerBlock, WaterConsumer.KEY)
        ).join();

        Block lavaProducerBlock = chunk.getBlock(3, 64, 5);
        FluidProducer lavaProducer = (FluidProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(lavaProducerBlock, LavaProducer.KEY)
        ).join();

        Block lavaConsumerBlock = chunk.getBlock(7, 64, 4);
        FluidConsumer lavaConsumer = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(lavaConsumerBlock, LavaConsumer.KEY)
        ).join();

        TestUtil.runSync(() -> {
            FluidManager.connect(waterProducer.getPoint(), waterConsumer.getPoint());
            FluidManager.setFluidPerSecond(waterProducer.getPoint().getSegment(), 1);

            FluidManager.connect(lavaProducer.getPoint(), lavaConsumer.getPoint());
            FluidManager.setFluidPerSecond(lavaProducer.getPoint().getSegment(), 2);
        }).join();

        TestUtil.sleepTicks(5).join();

        assertThat(waterConsumer.getAmount() * 2)
                .isGreaterThan(lavaConsumer.getAmount() - 0.1)
                .isLessThan(lavaConsumer.getAmount() + 0.1);

        chunk.setForceLoaded(false);
    }
}
