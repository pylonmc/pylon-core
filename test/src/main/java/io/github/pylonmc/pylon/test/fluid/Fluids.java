package io.github.pylonmc.pylon.test.fluid;

import io.github.pylonmc.rebar.fluid.PylonFluid;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Material;


public class Fluids {

    public static final PylonFluid WATER = new PylonFluid(PylonTest.key("water"),  Material.CYAN_CONCRETE);
    public static final PylonFluid LAVA = new PylonFluid(PylonTest.key("lava"), Material.ORANGE_CONCRETE)
            .addTag(new LavaTag());

    public static void register() {
        WATER.register();
        LAVA.register();
    }
}
