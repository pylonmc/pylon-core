package io.github.pylonmc.pylon.test.block;

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
import io.github.pylonmc.pylon.test.fluid.Fluids;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class WaterProducer extends PylonBlock<PylonBlockSchema> implements PylonFluidBlock, PylonUnloadBlock {

    private final NamespacedKey pointKey = PylonTest.key("point");
    private final NamespacedKey amountKey = PylonTest.key("amount");
    @Getter
    private final FluidConnectionPoint point;
    @Getter
    private int amount;

    @SuppressWarnings("unused")
    public WaterProducer (PylonBlockSchema schema, Block block, BlockCreateContext context) {
        super(schema, block);
        point = new FluidConnectionPoint(block, "output", FluidConnectionPoint.Type.OUTPUT);
        FluidManager.add(point);
        amount = 0;
    }

    @SuppressWarnings("unused")
    public WaterProducer(PylonBlockSchema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block);
        point = pdc.get(pointKey, PylonSerializers.FLUID_CONNECTION_POINT);
        FluidManager.add(point);
        amount = pdc.get(amountKey, PylonSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(pointKey, PylonSerializers.FLUID_CONNECTION_POINT, point);
        pdc.set(amountKey, PylonSerializers.INTEGER, amount);
    }

    @Override
    public void onUnload(@NotNull PylonBlockUnloadEvent event) {
        FluidManager.remove(point);
    }

    @Override
    public @NotNull Map<PylonFluid, Integer> getRequestedFluids(@NotNull String connectionPoint) {
        return Map.of(
                Fluids.WATER, 25
        );
    }

    @Override
    public void addFluid(@NotNull String connectionPoint, @NotNull PylonFluid fluid, int amount) {
        this.amount += amount;
    }
}
