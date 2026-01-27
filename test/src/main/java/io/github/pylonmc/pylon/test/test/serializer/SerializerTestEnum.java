package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.PylonSerializers;
import org.bukkit.Material;

public class SerializerTestEnum extends SerializerTest<Material> {
    public SerializerTestEnum() {
        super(Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, PylonSerializers.ENUM.enumTypeFrom(Material.class));
    }
}
