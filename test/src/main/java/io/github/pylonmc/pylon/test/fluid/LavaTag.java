package io.github.pylonmc.pylon.test.fluid;

import io.github.pylonmc.pylon.core.fluid.PylonFluidTag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


public class LavaTag implements PylonFluidTag {
    @Override
    public @NotNull Component getDisplayText() {
        return Component.empty();
    }
}
