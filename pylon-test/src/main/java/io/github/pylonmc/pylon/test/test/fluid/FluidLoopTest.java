package io.github.pylonmc.pylon.test.test.fluid;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.block.FluidConnector;
import io.github.pylonmc.pylon.test.block.WaterConsumer;
import io.github.pylonmc.pylon.test.block.WaterProducer;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;


public class FluidLoopTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Block producerBlock = chunk.getBlock(2, 64, 5);
        WaterProducer producer = (WaterProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(producerBlock, Blocks.WATER_PRODUCER)
        ).join();
        assertThat(producer)
                .isNotNull();

        Block connectorBlock = chunk.getBlock(4, 64, 5);
        FluidConnector connector = (FluidConnector) TestUtil.runSync(
                () -> BlockStorage.placeBlock(connectorBlock, Blocks.FLUID_CONNECTOR)
        ).join();
        assertThat(connector)
                .isNotNull();

        Block consumerBlock = chunk.getBlock(6, 64, 5);
        WaterConsumer consumer = (WaterConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(consumerBlock, Blocks.WATER_CONSUMER)
        ).join();
        assertThat(consumer)
                .isNotNull();

        // Create loop
        TestUtil.runSync(() -> {
            FluidManager.connect(consumer.getPoint(), producer.getPoint());
            FluidManager.connect(producer.getPoint(), connector.getPoint());
            FluidManager.connect(connector.getPoint(), consumer.getPoint());
        }).join();
        assertThat(consumer.getPoint().getSegment())
                .isEqualTo(producer.getPoint().getSegment())
                .isEqualTo(connector.getPoint().getSegment());

        // Disconnect one link; all should still have the same segment because they're connected in other ways still
        TestUtil.runSync(() -> FluidManager.disconnect(consumer.getPoint(), producer.getPoint())).join();
        assertThat(consumer.getPoint().getSegment())
                .isEqualTo(producer.getPoint().getSegment())
                .isEqualTo(connector.getPoint().getSegment());


        // Disconnect other links; all should now have distinct segments
        TestUtil.runSync(() -> {
            FluidManager.disconnect(producer.getPoint(), connector.getPoint());
            FluidManager.disconnect(connector.getPoint(), consumer.getPoint());
        }).join();
        assertThat(consumer.getPoint().getSegment())
                .isNotEqualTo(producer.getPoint().getSegment())
                .isNotEqualTo(connector.getPoint().getSegment());

        chunk.setForceLoaded(false);
    }
}
