package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import org.bukkit.Material;

public class SerializerTestEnum implements GenericTest {
    @Override
    public void run() {
        SerializerTests.testSerializer(
                Material.WAXED_WEATHERED_CUT_COPPER_STAIRS,
                PylonSerializers.ENUM.enumTypeFrom(Material.class)
        );
    }
}
