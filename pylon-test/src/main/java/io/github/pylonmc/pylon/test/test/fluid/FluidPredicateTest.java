package io.github.pylonmc.pylon.test.test.fluid;

import io.github.pylonmc.pylon.core.block.BlockStorage;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import io.github.pylonmc.pylon.test.block.Blocks;
import io.github.pylonmc.pylon.test.block.FluidConsumer;
import io.github.pylonmc.pylon.test.block.FluidProducer;
import io.github.pylonmc.pylon.test.fluid.LavaTag;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import static org.assertj.core.api.Assertions.assertThat;


public class FluidPredicateTest extends AsyncTest {

    @Override
    protected void test() {
        Chunk chunk = TestUtil.getRandomChunk(false).join();
        chunk.setForceLoaded(true);

        Block waterProducerBlock = chunk.getBlock(2, 64, 5);
        FluidProducer waterProducer = (FluidProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(waterProducerBlock, Blocks.WATER_PRODUCER)
        ).join();

        Block waterConsumerBlock = chunk.getBlock(6, 64, 4);
        FluidConsumer waterConsumer = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(waterConsumerBlock, Blocks.WATER_CONSUMER)
        ).join();

        Block lavaProducerBlock = chunk.getBlock(3, 64, 5);
        FluidProducer lavaProducer = (FluidProducer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(lavaProducerBlock, Blocks.LAVA_PRODUCER)
        ).join();

        Block lavaConsumerBlock = chunk.getBlock(7, 64, 4);
        FluidConsumer lavaConsumer = (FluidConsumer) TestUtil.runSync(
                () -> BlockStorage.placeBlock(lavaConsumerBlock, Blocks.LAVA_CONSUMER)
        ).join();

        TestUtil.runSync(() -> {
            FluidManager.connect(waterProducer.getPoint(), waterConsumer.getPoint());
            FluidManager.setFluidPredicate(waterProducer.getPoint().getSegment(), fluid -> fluid.hasTag(LavaTag.class));

            FluidManager.connect(lavaProducer.getPoint(), lavaConsumer.getPoint());
            FluidManager.setFluidPredicate(lavaProducer.getPoint().getSegment(), fluid -> fluid.hasTag(LavaTag.class));
            // also check that the predicate is preserved across connects/disconnects
            FluidManager.disconnect(lavaProducer.getPoint(), lavaConsumer.getPoint());
            FluidManager.connect(lavaProducer.getPoint(), lavaConsumer.getPoint());
        }).join();

        TestUtil.sleepTicks(5).join();

        assertThat(waterConsumer.getAmount())
                .isEqualTo(0);

        assertThat(lavaConsumer.getAmount())
                .isNotEqualTo(0);

        chunk.setForceLoaded(false);
    }
}
