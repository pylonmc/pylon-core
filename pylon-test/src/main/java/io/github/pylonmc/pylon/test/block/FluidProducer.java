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
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class FluidProducer extends PylonBlock<FluidProducer.Schema> implements PylonFluidBlock, PylonUnloadBlock {

    private final NamespacedKey pointKey = PylonTest.key("point");
    @Getter private final FluidConnectionPoint point;

    public static class Schema extends PylonBlockSchema {

        @Getter private final PylonFluid fluid;

        public Schema(@NotNull NamespacedKey key, @NotNull Material material, @NotNull PylonFluid fluid) {
            super(key, material, FluidProducer.class);
            this.fluid = fluid;
        }
    }

    @SuppressWarnings("unused")
    public FluidProducer(Schema schema, Block block, BlockCreateContext context) {
        super(schema, block);
        point = new FluidConnectionPoint(block, "output", FluidConnectionPoint.Type.OUTPUT);
        FluidManager.add(point);
    }

    @SuppressWarnings("unused")
    public FluidProducer(Schema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block);
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
    public @NotNull Map<PylonFluid, Long> getSuppliedFluids(@NotNull String connectionPoint) {
        return Map.of(
                getSchema().fluid, 20L
        );
    }

    @Override
    public void removeFluid(@NotNull String connectionPoint, @NotNull PylonFluid fluid, long amount) {
        // do nothing
    }
}
