package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import org.bukkit.Material;

public class SerializerTestEnum extends SerializerTest<Material> {
    public SerializerTestEnum() {
        super(Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, RebarSerializers.ENUM.enumTypeFrom(Material.class));
    }
}
