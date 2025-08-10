package io.github.pylonmc.pylon.test.block.fluid;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.base.PylonFluidBufferBlock;
import io.github.pylonmc.pylon.core.block.base.PylonUnloadBlock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.core.fluid.FluidPointType;
import io.github.pylonmc.pylon.core.fluid.PylonFluid;
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.fluid.Fluids;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class FluidConsumer extends PylonBlock implements PylonFluidBufferBlock, PylonUnloadBlock {

    public static final NamespacedKey LAVA_CONSUMER_KEY = PylonTest.key("lava_consumer");
    public static final NamespacedKey WATER_CONSUMER_KEY = PylonTest.key("water_consumer");

    private static final NamespacedKey pointKey = PylonTest.key("point");

    private static final double CAPACITY = 100.0;

    @Getter private final VirtualFluidPoint point;

    @SuppressWarnings("unused")
    public FluidConsumer(Block block, BlockCreateContext context) {
        super(block);
        point = new VirtualFluidPoint(block, FluidPointType.INPUT);
        FluidManager.add(point);
        createFluidBuffer(getFluidType(), CAPACITY, true, false);
    }

    @SuppressWarnings("unused")
    public FluidConsumer(Block block, PersistentDataContainer pdc) {
        super(block);
        point = pdc.get(pointKey, PylonSerializers.FLUID_CONNECTION_POINT);
        FluidManager.add(point);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(pointKey, PylonSerializers.FLUID_CONNECTION_POINT, point);
    }

    @Override
    public void onUnload(@NotNull PylonBlockUnloadEvent event) {
        FluidManager.remove(point);
    }

    public double getAmount() {
        return fluidAmount(getFluidType());
    }

    private PylonFluid getFluidType() {
        return Map.of(
                LAVA_CONSUMER_KEY, Fluids.LAVA,
                WATER_CONSUMER_KEY, Fluids.WATER
        ).get(getKey());
    }
}
