package io.github.pylonmc.pylon.test.block.fluid;

import io.github.pylonmc.pylon.core.block.PylonBlock;
import io.github.pylonmc.pylon.core.block.base.PylonUnloadBlock;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent;
import io.github.pylonmc.pylon.core.fluid.FluidManager;
import io.github.pylonmc.pylon.core.fluid.FluidPointType;
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint;
import io.github.pylonmc.pylon.test.PylonTest;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public class FluidConnector extends PylonBlock implements PylonUnloadBlock {

    public static final NamespacedKey KEY = PylonTest.key("fluid_connector");
    private static final NamespacedKey POINT_KEY = PylonTest.key("point");

    @Getter
    private final VirtualFluidPoint point;

    @SuppressWarnings("unused")
    public FluidConnector(Block block, BlockCreateContext context) {
        super(block);
        point = new VirtualFluidPoint(block, FluidPointType.INTERSECTION);
        FluidManager.add(point);
    }

    @SuppressWarnings("unused")
    public FluidConnector(Block block, PersistentDataContainer pdc) {
        super(block);
        point = pdc.get(POINT_KEY, PylonSerializers.FLUID_CONNECTION_POINT);
        FluidManager.add(point);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(POINT_KEY, PylonSerializers.FLUID_CONNECTION_POINT, point);
    }

    @Override
    public void onUnload(@NotNull PylonBlockUnloadEvent event) {
        FluidManager.remove(point);
    }
}
