package io.github.pylonmc.rebar.test.fluid;

import io.github.pylonmc.rebar.fluid.RebarFluidTag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


public class LavaTag implements RebarFluidTag {
    @Override
    public @NotNull Component getDisplayText() {
        return Component.empty();
    }
}
