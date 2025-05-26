package io.github.pylonmc.pylon.test.test.fluid;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.block.FluidConsumer;
import io.github.pylonmc.pylon.test.block.FluidLimiter;
import io.github.pylonmc.pylon.test.block.FluidProducer;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;


public class FluidTickerLoopTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Block producerBlock = chunk.getBlock(2, 64, 5);
        FluidProducer producer = (FluidProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(producerBlock, Blocks.WATER_PRODUCER)
        ).join();

        Block limiterBlock1 = chunk.getBlock(4, 64, 4);
        FluidLimiter limiter1 = (FluidLimiter) TestUtil.runSync(
                () -> BlockStorage.placeBlock(limiterBlock1, Blocks.FLUID_LIMITER)
        ).join();

        Block limiterBlock2 = chunk.getBlock(4, 64, 6);
        FluidLimiter limiter2 = (FluidLimiter) TestUtil.runSync(
                () -> BlockStorage.placeBlock(limiterBlock2, Blocks.FLUID_LIMITER)
        ).join();

        Block consumerBlock = chunk.getBlock(6, 64, 4);
        FluidConsumer consumer = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(consumerBlock, Blocks.WATER_CONSUMER)
        ).join();

        TestUtil.runSync(() -> {
            FluidManager.connect(producer.getPoint(), limiter1.getInput());
            FluidManager.connect(limiter2.getOutput(), limiter1.getInput());
            FluidManager.connect(consumer.getPoint(), limiter2.getInput());
            FluidManager.connect(limiter1.getOutput(), limiter2.getInput());
        }).join();

        TestUtil.sleepTicks(20).join();

        // account for +- 0.1 floating point offset, divide by 2 because half the input should go to the limiter loop
        assertThat(consumer.getAmount())
                .isGreaterThanOrEqualTo(Blocks.FLUID_LIMITER.getMaxFlowRate() / 2 - 0.1)
                .isLessThanOrEqualTo(Blocks.FLUID_LIMITER.getMaxFlowRate() / 2 + 0.1);

        chunk.setForceLoaded(false);
    }
}
