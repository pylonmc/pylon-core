package io.github.pylonmc.pylon.test.test.fluid;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.fluid.consumer.WaterConsumer;
import io.github.pylonmc.pylon.test.block.fluid.producer.FluidProducer;
import io.github.pylonmc.pylon.test.block.fluid.consumer.FluidConsumer;
import io.github.pylonmc.pylon.test.block.fluid.producer.WaterProducer;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;


public class FluidConnectionTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Block consumerBlock = chunk.getBlock(6, 64, 5);
        FluidConsumer consumer = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(consumerBlock, WaterConsumer.KEY)
        ).join();
        assertThat(consumer)
                .isNotNull();

        Block producerBlock = chunk.getBlock(2, 64, 5);
        FluidProducer producer = (FluidProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(producerBlock, WaterProducer.KEY)
        ).join();
        assertThat(producer)
                .isNotNull();

        // Both should initially be disconnected
        assertThat(consumer.getPoint().getConnectedPoints())
                .isEmpty();
        assertThat(producer.getPoint().getConnectedPoints())
                .isEmpty();
        assertThat(producer.getPoint().getSegment())
                .isNotEqualTo(consumer.getPoint().getSegment());

        // Connect
        TestUtil.runSync(() -> FluidManager.connect(consumer.getPoint(), producer.getPoint())).join();
        assertThat(consumer.getPoint().getConnectedPoints())
                .containsExactly(producer.getPoint().getId());
        assertThat(producer.getPoint().getConnectedPoints())
                .containsExactly(consumer.getPoint().getId());
        assertThat(producer.getPoint().getSegment())
                .isEqualTo(consumer.getPoint().getSegment());

        // Disconnect
        TestUtil.runSync(() -> FluidManager.disconnect(consumer.getPoint(), producer.getPoint())).join();
        assertThat(consumer.getPoint().getConnectedPoints())
                .isEmpty();
        assertThat(producer.getPoint().getConnectedPoints())
                .isEmpty();
        assertThat(producer.getPoint().getSegment())
                .isNotEqualTo(consumer.getPoint().getSegment());

        // Yeet
        TestUtil.runSync(() -> {
            BlockStorage.breakBlock(consumerBlock);
            BlockStorage.breakBlock(producerBlock);
        }).join();

        chunk.setForceLoaded(false);
    }
}
