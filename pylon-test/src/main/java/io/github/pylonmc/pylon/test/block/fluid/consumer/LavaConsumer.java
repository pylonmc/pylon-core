package io.github.pylonmc.pylon.test.block.fluid.consumer;

import io.github.pylonmc.pylon.core.block.PylonBlockSchema;
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext;
import io.github.pylonmc.pylon.core.fluid.PylonFluid;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.fluid.Fluids;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;


public class LavaConsumer extends FluidConsumer {

    public static final NamespacedKey KEY = PylonTest.key("lava_consumer");

    @SuppressWarnings("unused")
    public LavaConsumer(PylonBlockSchema schema, Block block, BlockCreateContext context) {
        super(schema, block, context);
    }

    @SuppressWarnings("unused")
    public LavaConsumer(PylonBlockSchema schema, Block block, PersistentDataContainer pdc) {
        super(schema, block, pdc);
    }

    @Override
    PylonFluid getFluid() {
        return Fluids.LAVA;
    }

    @Override
    double getCapacity() {
        return 100;
    }
}
