package io.github.pylonmc.pylon.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.PylonSerializers;
import org.bukkit.util.Vector;


public class SerializerTestVector extends SerializerTest<Vector> {
    public SerializerTestVector() {
        super(new Vector(1, 20, -21), PylonSerializers.VECTOR);
    }
}
