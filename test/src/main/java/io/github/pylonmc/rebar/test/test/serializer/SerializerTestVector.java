package io.github.pylonmc.rebar.test.test.serializer;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import org.bukkit.util.Vector;


public class SerializerTestVector extends SerializerTest<Vector> {
    public SerializerTestVector() {
        super(new Vector(1, 20, -21), RebarSerializers.VECTOR);
    }
}
