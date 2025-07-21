package io.github.pylonmc.pylon.test.block.fluid;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.base.PylonFluidBlock;
import io.github.pylonmc.pylon.core.block.base.PylonUnloadBlock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent;
import io.github.pylonmc.pylon.core.fluid.FluidPointType;
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.core.fluid.PylonFluid;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.fluid.Fluids;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class FluidProducer extends PylonBlock implements PylonFluidBlock, PylonUnloadBlock {

    public static final NamespacedKey LAVA_PRODUCER_KEY = PylonTest.key("lava_producer");
    public static final NamespacedKey WATER_PRODUCER_KEY = PylonTest.key("water_producer");

    public static final double FLUID_PER_SECOND = 200.0;

    private final NamespacedKey pointKey = PylonTest.key("point");
    @Getter private final VirtualFluidPoint point;

    @SuppressWarnings("unused")
    public FluidProducer(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
        point = new VirtualFluidPoint(block, FluidPointType.OUTPUT);
        FluidManager.add(point);
    }

    @SuppressWarnings("unused")
    public FluidProducer(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
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

    @Override
    public @NotNull Map<PylonFluid, Double> getSuppliedFluids(double deltaSeconds) {
        return Map.of(
                getFluidType(), FLUID_PER_SECOND * deltaSeconds
        );
    }

    @Override
    public void onFluidRemoved(@NotNull PylonFluid fluid, double amount) {
        // do nothing
    }

    private PylonFluid getFluidType() {
        return Map.of(
                LAVA_PRODUCER_KEY, Fluids.LAVA,
                WATER_PRODUCER_KEY, Fluids.WATER
        ).get(getKey());
    }
}
