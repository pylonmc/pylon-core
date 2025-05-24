package io.github.pylonmc.pylon.test.block.fluid.consumer;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.base.PylonFluidBlock;
import io.github.pylonmc.pylon.core.block.base.PylonUnloadBlock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent;
import io.github.pylonmc.pylon.core.fluid.FluidConnectionPoint;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.core.fluid.PylonFluid;
import io.github.pylonmc.pylon.test.PylonTest;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public abstract class FluidConsumer extends PylonBlock implements PylonFluidBlock, PylonUnloadBlock {

    private static final NamespacedKey pointKey = PylonTest.key("point");
    private static final NamespacedKey amountKey = PylonTest.key("amount");

    @Getter private final FluidConnectionPoint point;
    @Getter private double amount;

    abstract PylonFluid getFluid();
    abstract double getCapacity();

    @SuppressWarnings("unused")
    protected FluidConsumer(PylonBlockSchema schema, Block block, BlockCreateContext context) {
        super(schema, block);
        point = new FluidConnectionPoint(block, "output", FluidConnectionPoint.Type.INPUT);
        FluidManager.add(point);
        amount = 0;
    }

    @SuppressWarnings("unused")
    protected FluidConsumer(PylonBlockSchema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block);
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
    public @NotNull Map<PylonFluid, Double> getRequestedFluids(@NotNull String connectionPoint, double deltaSeconds) {
        return Map.of(
                getFluid(), getCapacity() - amount
        );
    }

    @Override
    public void addFluid(@NotNull String connectionPoint, @NotNull PylonFluid fluid, double amount) {
        this.amount += amount;
    }
}
