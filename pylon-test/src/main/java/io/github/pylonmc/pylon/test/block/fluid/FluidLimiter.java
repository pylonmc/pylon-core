package io.github.pylonmc.pylon.test.block.fluid;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.base.PylonFluidBlock;
import io.github.pylonmc.pylon.core.block.base.PylonUnloadBlock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.core.fluid.FluidPointType;
import io.github.pylonmc.pylon.core.fluid.PylonFluid;
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.util.PdcUtils;
import io.github.pylonmc.pylon.test.PylonTest;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class FluidLimiter extends PylonBlock implements PylonFluidBlock, PylonUnloadBlock {

    public static final NamespacedKey KEY = PylonTest.key("fluid_limiter");
    private static final NamespacedKey INPUT_KEY = PylonTest.key("input");
    private static final NamespacedKey OUTPUT_KEY = PylonTest.key("output");
    private static final NamespacedKey FLUID_KEY = PylonTest.key("fluid");
    private static final NamespacedKey AMOUNT_KEY = PylonTest.key("amount");

    public static final double MAX_FLOW_RATE = 50.0;

    @Getter private final VirtualFluidPoint input;
    @Getter private final VirtualFluidPoint output;
    @Getter private @Nullable PylonFluid fluid;
    @Getter private double amount;

    @SuppressWarnings("unused")
    public FluidLimiter(Block block, BlockCreateContext context) {
        super(block);

        input = new VirtualFluidPoint(block, FluidPointType.INPUT);
        output = new VirtualFluidPoint(block, FluidPointType.OUTPUT);
        fluid = null;
        amount = 0;

        FluidManager.add(input);
        FluidManager.add(output);
    }

    @SuppressWarnings("unused")
    public FluidLimiter(Block block, PersistentDataContainer pdc) {
        super(block);

        input = pdc.get(INPUT_KEY, PylonSerializers.FLUID_CONNECTION_POINT);
        output = pdc.get(OUTPUT_KEY, PylonSerializers.FLUID_CONNECTION_POINT);
        fluid = pdc.get(FLUID_KEY, PylonSerializers.PYLON_FLUID);
        amount = pdc.get(AMOUNT_KEY, PylonSerializers.DOUBLE);

        FluidManager.add(input);
        FluidManager.add(output);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(INPUT_KEY, PylonSerializers.FLUID_CONNECTION_POINT, input);
        pdc.set(OUTPUT_KEY, PylonSerializers.FLUID_CONNECTION_POINT, output);
        PdcUtils.setNullable(pdc, FLUID_KEY, PylonSerializers.PYLON_FLUID, fluid);
        pdc.set(AMOUNT_KEY, PylonSerializers.DOUBLE, amount);
    }

    @Override
    public void onUnload(@NotNull PylonBlockUnloadEvent event) {
        FluidManager.remove(input);
        FluidManager.remove(output);
    }

    @Override
    public @NotNull Map<PylonFluid, Double> getRequestedFluids(double deltaSeconds) {
        return amount == 0
                ? PylonRegistry.FLUIDS.getValues()
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), key -> MAX_FLOW_RATE * deltaSeconds))
                : Map.of();
    }

    @Override
    public void addFluid(@NotNull PylonFluid fluid, double amount) {
        this.fluid = fluid;
        this.amount += amount;
    }

    @Override
    public @NotNull Map<PylonFluid, Double> getSuppliedFluids(double deltaSeconds) {
        return fluid == null
                ? Map.of()
                : Map.of(fluid, amount);
    }

    @Override
    public void removeFluid(@NotNull PylonFluid fluid, double amount) {
        this.amount -= amount;
        if (this.amount == 0) {
            this.fluid = null;
        }
    }
}
