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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class FluidConsumer extends PylonBlock<FluidConsumer.Schema> implements PylonFluidBlock, PylonUnloadBlock {

    private final NamespacedKey pointKey = PylonTest.key("point");
    private final NamespacedKey amountKey = PylonTest.key("amount");
    @Getter private final FluidConnectionPoint point;
    @Getter private long amount;

    public static class Schema extends PylonBlockSchema {

        private final PylonFluid fluid;
        private final long capacity;

        public Schema(@NotNull NamespacedKey key, @NotNull Material material, @NotNull PylonFluid fluid, long capacity) {
            super(key, material, FluidConsumer.class);
            this.fluid = fluid;
            this.capacity = capacity;
        }
    }

    @SuppressWarnings("unused")
    public FluidConsumer(Schema schema, Block block, BlockCreateContext context) {
        super(schema, block);
        point = new FluidConnectionPoint(block, "output", FluidConnectionPoint.Type.INPUT);
        FluidManager.add(point);
        amount = 0;
    }

    @SuppressWarnings("unused")
    public FluidConsumer(Schema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block);
        point = pdc.get(pointKey, PylonSerializers.FLUID_CONNECTION_POINT);
        FluidManager.add(point);
        amount = pdc.get(amountKey, PylonSerializers.LONG);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(pointKey, PylonSerializers.FLUID_CONNECTION_POINT, point);
        pdc.set(amountKey, PylonSerializers.LONG, amount);
    }

    @Override
    public void onUnload(@NotNull PylonBlockUnloadEvent event) {
        FluidManager.remove(point);
    }

    @Override
    public @NotNull Map<PylonFluid, Long> getRequestedFluids(@NotNull String connectionPoint) {
        return Map.of(
                getSchema().fluid, getSchema().capacity - amount
        );
    }

    @Override
    public void addFluid(@NotNull String connectionPoint, @NotNull PylonFluid fluid, long amount) {
        this.amount += amount;
    }
}
