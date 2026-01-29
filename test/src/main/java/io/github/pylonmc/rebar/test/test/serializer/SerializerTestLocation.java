package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.test.RebarTest;
import org.bukkit.Location;

public class SerializerTestLocation extends SerializerTest<Location> {
    public SerializerTestLocation() {
        super(new Location(RebarTest.testWorld, 5.0, 20.0, 13.0), RebarSerializers.LOCATION);
    }
}
