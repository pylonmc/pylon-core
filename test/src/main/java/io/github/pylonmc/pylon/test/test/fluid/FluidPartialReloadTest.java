package io.github.pylonmc.pylon.test.test.fluid;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.fluid.FluidManager;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.fluid.FluidConnector;
import io.github.pylonmc.pylon.test.block.fluid.FluidConsumer;
import io.github.pylonmc.pylon.test.block.fluid.FluidProducer;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;


public class FluidPartialReloadTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk producerChunk = TestUtil.getRandomChunk(false).join();
        producerChunk.setForceLoaded(true);
        Chunk connectorChunk = TestUtil.getRandomChunk(false).join();
        connectorChunk.setForceLoaded(true);
        Chunk consumerChunk = TestUtil.getRandomChunk(false).join();
        consumerChunk.setForceLoaded(true);

        Block consumerBlock = consumerChunk.getBlock(6, 64, 5);
        FluidConsumer consumer = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(consumerBlock, FluidConsumer.WATER_CONSUMER_KEY)
        ).join();

        Block connectorBlock = connectorChunk.getBlock(8, 64, 3);
        FluidConnector connector = (FluidConnector) TestUtil.runSync(
                () -> BlockStorage.placeBlock(connectorBlock, FluidConnector.KEY)
        ).join();

        Block producerBlock = producerChunk.getBlock(2, 64, 5);
        FluidProducer producer = (FluidProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(producerBlock, FluidProducer.WATER_PRODUCER_KEY)
        ).join();

        TestUtil.runSync(() -> {
            FluidManager.connect(consumer.getPoint(), connector.getPoint());
            FluidManager.connect(producer.getPoint(), connector.getPoint());
        }).join();

        // All should initially have the same segment
        assertThat(consumer.getPoint().getSegment())
                .isEqualTo(connector.getPoint().getSegment())
                .isEqualTo(producer.getPoint().getSegment());

        // After unloading the connector, the producer and consumer should have different segments
        connectorChunk.setForceLoaded(false);
        TestUtil.unloadChunk(connectorChunk).join();
        assertThat(consumer.getPoint().getSegment())
                .isNotEqualTo(producer.getPoint().getSegment());

        // When the chunk is reloaded, all should have the same segment again
        TestUtil.loadChunk(connectorChunk).join();
        connectorChunk.setForceLoaded(true);
        FluidConnector reloadedConnector = BlockStorage.getAs(FluidConnector.class, connectorBlock);
        assertThat(consumer.getPoint().getSegment())
                .isEqualTo(reloadedConnector.getPoint().getSegment())
                .isEqualTo(producer.getPoint().getSegment());

        producerChunk.setForceLoaded(false);
        connectorChunk.setForceLoaded(false);
        consumerChunk.setForceLoaded(false);
    }
}
