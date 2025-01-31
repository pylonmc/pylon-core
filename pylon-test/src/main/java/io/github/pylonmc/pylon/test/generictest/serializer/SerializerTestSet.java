package io.github.pylonmc.pylon.test.generictest.serializer;

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers;
import io.github.pylonmc.pylon.test.GenericTest;

import java.util.Set;

public class SerializerTestSet implements GenericTest {
    @Override
    public void run() {
        // Set of integers
        {
            var type = PylonSerializers.SET.setTypeFrom(PylonSerializers.INTEGER);
            Set<Integer> value = Set.of(1, 2, 20, 40);

            SerializerTests.testSerializer(value, type);
        }

        // Set of set of strings
        {
            var type = PylonSerializers.SET.setTypeFrom(
                    PylonSerializers.SET.setTypeFrom(PylonSerializers.STRING)
            );
            Set<Set<String>> value = Set.of(Set.of("bruh", "bruuuu"), Set.of("*screaming*"), Set.of());

            SerializerTests.testSerializer(value, type);
        }
    }
}
