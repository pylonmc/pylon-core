package io.github.pylonmc.pylon.test.entity;

import io.github.pylonmc.pylon.core.entity.PylonEntitySchema;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.entity.LivingEntity;


public final class Entities {

    private Entities() {}

    public static final PylonEntitySchema SIMPLE_ENTITY = new PylonEntitySchema(
            PylonTest.key("simple_entity"),
            LivingEntity.class,
            SimpleEntity.class
    );
}
