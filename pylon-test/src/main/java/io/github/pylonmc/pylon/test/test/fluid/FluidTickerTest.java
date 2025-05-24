package io.github.pylonmc.pylon.test.test.fluid;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.fluid.FluidConnector;
import io.github.pylonmc.pylon.test.block.fluid.consumer.WaterConsumer;
import io.github.pylonmc.pylon.test.block.fluid.producer.FluidProducer;
import io.github.pylonmc.pylon.test.block.fluid.consumer.FluidConsumer;
import io.github.pylonmc.pylon.test.block.fluid.producer.WaterProducer;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;


public class FluidTickerTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Block producerBlock = chunk.getBlock(2, 64, 5);
        FluidProducer producer = (FluidProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(producerBlock, WaterProducer.KEY)
        ).join();

        Block connectorBlock = chunk.getBlock(4, 64, 5);
        FluidConnector connector = (FluidConnector) TestUtil.runSync(
                () -> BlockStorage.placeBlock(connectorBlock, FluidConnector.KEY)
        ).join();

        Block consumerBlock1 = chunk.getBlock(6, 64, 4);
        FluidConsumer consumer1 = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(consumerBlock1, WaterConsumer.KEY)
        ).join();

        Block consumerBlock2 = chunk.getBlock(6, 64, 6);
        FluidConsumer consumer2 = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(consumerBlock2, WaterConsumer.KEY)
        ).join();

        TestUtil.runSync(() -> {
            FluidManager.connect(producer.getPoint(), connector.getPoint());
            FluidManager.connect(connector.getPoint(), consumer1.getPoint());
            FluidManager.connect(connector.getPoint(), consumer2.getPoint());
        }).join();

        TestUtil.sleepTicks(20).join();

        assertThat(consumer1.getAmount())
                .isGreaterThanOrEqualTo(99.9)
                .isLessThanOrEqualTo(100.1);

        assertThat(consumer2.getAmount())
                .isGreaterThanOrEqualTo(99.9)
                .isLessThanOrEqualTo(100.1);

        chunk.setForceLoaded(false);
    }
}
