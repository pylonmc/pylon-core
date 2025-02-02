package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;
import io.github.pylonmc.pylon.test.TestAddon;
import org.bukkit.Location;

public class SerializerTestLocation implements GenericTest {
    @Override
    public void run() {
        Location value = new Location(TestAddon.testWorld, 5.0, 320.3, 38904.43443);
        value.setPitch(3.0F);
        value.setYaw(1.04F);

        SerializerTests.testSerializer(value, PylonSerializers.LOCATION);
    }
}
