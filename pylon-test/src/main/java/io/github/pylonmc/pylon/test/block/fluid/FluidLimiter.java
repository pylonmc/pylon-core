package io.github.pylonmc.pylon.test.block.fluid;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.base.PylonFluidTank;
import io.github.pylonmc.pylon.core.block.base.PylonUnloadBlock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent;
import io.github.pylonmc.pylon.core.fluid.FluidPointType;
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.core.fluid.PylonFluid;
import io.github.pylonmc.pylon.test.PylonTest;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public class FluidLimiter extends PylonBlock implements PylonFluidTank, PylonUnloadBlock {

    public static final NamespacedKey KEY = PylonTest.key("fluid_limiter");
    private static final NamespacedKey INPUT_KEY = PylonTest.key("input");
    private static final NamespacedKey OUTPUT_KEY = PylonTest.key("output");

    public static final double MAX_FLOW_RATE = 50.0;

    @Getter private final VirtualFluidPoint input;
    @Getter private final VirtualFluidPoint output;

    @SuppressWarnings("unused")
    public FluidLimiter(Block block, BlockCreateContext context) {
        super(block);

        input = new VirtualFluidPoint(block, FluidPointType.INPUT);
        output = new VirtualFluidPoint(block, FluidPointType.OUTPUT);

        FluidManager.add(input);
        FluidManager.add(output);
    }

    @SuppressWarnings("unused")
    public FluidLimiter(Block block, PersistentDataContainer pdc) {
        super(block);

        input = pdc.get(INPUT_KEY, PylonSerializers.FLUID_CONNECTION_POINT);
        output = pdc.get(OUTPUT_KEY, PylonSerializers.FLUID_CONNECTION_POINT);

        FluidManager.add(input);
        FluidManager.add(output);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(INPUT_KEY, PylonSerializers.FLUID_CONNECTION_POINT, input);
        pdc.set(OUTPUT_KEY, PylonSerializers.FLUID_CONNECTION_POINT, output);
    }

    @Override
    public void onUnload(@NotNull PylonBlockUnloadEvent event) {
        FluidManager.remove(input);
        FluidManager.remove(output);
    }

    @Override
    public boolean isAllowedFluid(@NotNull PylonFluid fluid) {
        return true;
    }
}
