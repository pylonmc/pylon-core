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


public class FluidConsumer extends PylonBlock implements PylonFluidBlock, PylonUnloadBlock {

    public static final NamespacedKey LAVA_CONSUMER_KEY = PylonTest.key("lava_consumer");
    public static final NamespacedKey WATER_CONSUMER_KEY = PylonTest.key("water_consumer");

    private static final NamespacedKey pointKey = PylonTest.key("point");
    private static final NamespacedKey amountKey = PylonTest.key("amount");

    private static final double CAPACITY = 100.0;

    @Getter private final VirtualFluidPoint point;
    @Getter private double amount;

    @SuppressWarnings("unused")
    public FluidConsumer(Block block, BlockCreateContext context) {
        super(block);
        point = new VirtualFluidPoint(block, FluidPointType.INPUT);
        FluidManager.add(point);
        amount = 0;
    }

    @SuppressWarnings("unused")
    public FluidConsumer(Block block, PersistentDataContainer pdc) {
        super(block);
        point = pdc.get(pointKey, PylonSerializers.FLUID_CONNECTION_POINT);
        FluidManager.add(point);
        amount = pdc.get(amountKey, PylonSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(pointKey, PylonSerializers.FLUID_CONNECTION_POINT, point);
        pdc.set(amountKey, PylonSerializers.DOUBLE, amount);
    }

    @Override
    public void onUnload(@NotNull PylonBlockUnloadEvent event) {
        FluidManager.remove(point);
    }

    @Override
    public @NotNull Map<PylonFluid, Double> getRequestedFluids(double deltaSeconds) {
        return Map.of(
                getFluidType(), CAPACITY - amount
        );
    }

    @Override
    public void addFluid(@NotNull PylonFluid fluid, double amount) {
        this.amount += amount;
    }

    private PylonFluid getFluidType() {
        return Map.of(
                LAVA_CONSUMER_KEY, Fluids.LAVA,
                WATER_CONSUMER_KEY, Fluids.WATER
        ).get(getKey());
    }
}
