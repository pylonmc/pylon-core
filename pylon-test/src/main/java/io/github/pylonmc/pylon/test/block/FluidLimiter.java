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
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.util.PdcUtils;
import io.github.pylonmc.pylon.test.PylonTest;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class FluidLimiter extends PylonBlock<FluidLimiter.Schema> implements PylonFluidBlock, PylonUnloadBlock {

    private final NamespacedKey inputKey = PylonTest.key("input");
    private final NamespacedKey outputKey = PylonTest.key("output");
    private final NamespacedKey fluidKey = PylonTest.key("fluid");
    private final NamespacedKey amountKey = PylonTest.key("amount");
    @Getter private final FluidConnectionPoint input;
    @Getter private final FluidConnectionPoint output;
    @Getter private @Nullable PylonFluid fluid;
    @Getter private double amount;

    public static class Schema extends PylonBlockSchema {

        @Getter private final double maxFlowRate;

        public Schema(@NotNull NamespacedKey key, @NotNull Material material, double maxFlowRate) {
            super(key, material, FluidLimiter.class);
            this.maxFlowRate = maxFlowRate;
        }
    }

    @SuppressWarnings("unused")
    public FluidLimiter(Schema schema, Block block, BlockCreateContext context) {
        super(schema, block);

        input = new FluidConnectionPoint(block, "input", FluidConnectionPoint.Type.INPUT);
        output = new FluidConnectionPoint(block, "output", FluidConnectionPoint.Type.OUTPUT);
        fluid = null;
        amount = 0;

        FluidManager.add(input);
        FluidManager.add(output);
    }

    @SuppressWarnings("unused")
    public FluidLimiter(Schema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block);

        input = pdc.get(inputKey, PylonSerializers.FLUID_CONNECTION_POINT);
        output = pdc.get(outputKey, PylonSerializers.FLUID_CONNECTION_POINT);
        fluid = pdc.get(fluidKey, PylonSerializers.PYLON_FLUID);
        amount = pdc.get(amountKey, PylonSerializers.DOUBLE);

        FluidManager.add(input);
        FluidManager.add(output);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(inputKey, PylonSerializers.FLUID_CONNECTION_POINT, input);
        pdc.set(outputKey, PylonSerializers.FLUID_CONNECTION_POINT, output);
        PdcUtils.setNullable(pdc, fluidKey, PylonSerializers.PYLON_FLUID, fluid);
        pdc.set(amountKey, PylonSerializers.DOUBLE, amount);
    }

    @Override
    public void onUnload(@NotNull PylonBlockUnloadEvent event) {
        FluidManager.remove(input);
        FluidManager.remove(output);
    }

    @Override
    public @NotNull Map<PylonFluid, Double> getRequestedFluids(@NotNull String connectionPoint, double deltaSeconds) {
        return amount == 0
                ? PylonRegistry.FLUIDS.getValues()
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), key -> getSchema().maxFlowRate * deltaSeconds))
                : Map.of();
    }

    @Override
    public void addFluid(@NotNull String connectionPoint, @NotNull PylonFluid fluid, double amount) {
        this.fluid = fluid;
        this.amount += amount;
    }

    @Override
    public @NotNull Map<PylonFluid, Double> getSuppliedFluids(@NotNull String connectionPoint, double deltaSeconds) {
        return fluid == null
                ? Map.of()
                : Map.of(fluid, amount);
    }

    @Override
    public void removeFluid(@NotNull String connectionPoint, @NotNull PylonFluid fluid, double amount) {
        this.amount -= amount;
        if (this.amount == 0) {
            this.fluid = null;
        }
    }
}
