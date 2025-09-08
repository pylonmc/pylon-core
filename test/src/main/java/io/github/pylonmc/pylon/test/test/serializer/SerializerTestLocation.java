package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Location;

public class SerializerTestLocation extends SerializerTest<Location> {
    public SerializerTestLocation() {
        super(new Location(PylonTest.testWorld, 5.0, 20.0, 13.0), PylonSerializers.LOCATION);
    }
}
